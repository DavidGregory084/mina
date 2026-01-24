/*
 * SPDX-FileCopyrightText:  © 2026 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.optimiser;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.multimap.set.MutableSetMultimap;
import org.eclipse.collections.impl.factory.Multimaps;
import org.mina_lang.common.names.Named;
import org.mina_lang.common.operators.BinaryOp;
import org.mina_lang.common.types.QuantifiedType;
import org.mina_lang.common.types.Type;
import org.mina_lang.ina.*;
import org.mina_lang.ina.Boolean;
import org.mina_lang.ina.Double;
import org.mina_lang.ina.Float;
import org.mina_lang.ina.Long;
import org.mina_lang.optimiser.constants.*;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class ConstantPropagation {
    private final Map<Named, Result> environment;
    private final Queue<Named> worklist;
    private final Map<Named, Expression> letBodies;
    private final MutableSetMultimap<Named, Named> occurrences;
    private final Map<Named, ImmutableList<Param>> funParams;
    private final FreeVariablesFolder freeVarsFolder;
    private long envState;

    ConstantPropagation(
        Map<Named, Result> environment,
        Queue<Named> worklist,
        Map<Named, Expression> letBodies,
        MutableSetMultimap<Named, Named> occurrences,
        Map<Named, ImmutableList<Param>> funParams
    ) {
        this.environment = environment;
        this.worklist = worklist;
        this.letBodies = letBodies;
        this.occurrences = occurrences;
        this.funParams = funParams;
        this.freeVarsFolder = new FreeVariablesFolder();
        this.envState = 0L;
    }

    ConstantPropagation(Map<Named, Result> environment) {
        this(
            environment,
            new ArrayDeque<>(),
            new HashMap<>(),
            Multimaps.mutable.set.empty(),
            new HashMap<>());
    }

    public ConstantPropagation() {
        this(new HashMap<>());
    }

    public Map<Named, Result> getEnvironment() {
        return environment;
    }

    private Result putResult(Named name, Result newValue) {
        return environment.compute(name, (n, existingValue) -> {
            if (existingValue == null) {
                // We have information about this key for the first time
                envState++;
                return newValue;
            } else {
                if (newValue.compare(existingValue) > 0.0) {
                    // We gained more information about this key
                    envState++;
                }

                return Result.leastUpperBound(existingValue, newValue);
            }
        });
    }

    public Namespace optimiseNamespace(Namespace namespace) {
        return new Namespace(namespace.name(), optimiseDeclarations(namespace.declarations()));
    }

    ImmutableList<Declaration> optimiseDeclarations(ImmutableList<Declaration> declarations) {
        analyseDeclarations(declarations);
        return declarations.collect(this::optimiseDeclaration);
    }

    Declaration optimiseDeclaration(Declaration declaration) {
        if (declaration instanceof Data data) {
            return data;
        } else if (declaration instanceof Let let) {
            return new Let(let.name(), let.type(), optimiseExpression(let.body()));
        }

        return declaration;
    }

    Expression optimiseExpression(Expression expr) {
        if (expr instanceof Apply apply) {
            if (apply.expr() instanceof Reference ref &&
                environment.get(ref.name()) instanceof Constant constant) {
                return constant.value();
            } else if (apply.expr() instanceof Reference ref &&
                environment.get(ref.name()) instanceof ConstantConstructor constant) {
                return new Apply(
                    apply.type(),
                    new Reference(constant.constructor(), apply.type()),
                    Lists.immutable.empty());
            } else {
                return new Apply(
                    apply.type(),
                    optimiseValue(apply.expr()),
                    apply.args().collect(this::optimiseValue));
            }
        } else if (expr instanceof BinOp binOp) {
            var leftExpr = analyseExpression(binOp.left());
            var rightExpr = analyseExpression(binOp.right());
            if (leftExpr instanceof Constant left &&
                rightExpr instanceof Constant right &&
                evaluateBinOp(binOp, left, right) instanceof Constant constant) {
                return constant.value();
            } else {
                return new BinOp(
                    binOp.type(),
                    optimiseValue(binOp.left()),
                    binOp.operator(),
                    optimiseValue(binOp.right()));
            }
        } else if (expr instanceof Block block) {
            var bindings = block.bindings().flatCollect(this::optimiseBinding);
            var result = optimiseExpression(block.result());
            return bindings.isEmpty() ? result : new Block(block.type(), bindings, result);
        } else if (expr instanceof If ifExpr) {
            var cond = analyseExpression(ifExpr.condition());
            if (cond instanceof Constant constant &&
                constant.value() instanceof Boolean bool) {
                return bool.value()
                    ? optimiseExpression(ifExpr.consequent())
                    : optimiseExpression(ifExpr.alternative());
            } else {
                return new If(
                    ifExpr.type(),
                    optimiseValue(ifExpr.condition()),
                    optimiseExpression(ifExpr.consequent()),
                    optimiseExpression(ifExpr.alternative()));
            }
        } else if (expr instanceof Match match) {
            var scrutinee = analyseExpression(match.scrutinee());
            if (scrutinee instanceof Constant constant) {
                var matchingCase = match.cases().detect(cse -> {
                    var result = analysePattern(cse.pattern());
                    var isMatchingConstant = constant.equals(result);
                    var isUnknown = result == Unknown.VALUE;
                    return isMatchingConstant || isUnknown;
                });
                return matchingCase != null
                    ? matchingCase.consequent()
                    : new Match(
                        match.type(),
                        optimiseValue(match.scrutinee()),
                        match.cases().collect(this::optimiseCase));
            } else if (scrutinee instanceof ConstructorResult known) {
                var matchingCases = matchingCases(match.cases(), known);
                return matchingCases.size() == 1
                    ? matchingCases.getFirst().consequent()
                    : new Match(
                        match.type(),
                        optimiseValue(match.scrutinee()),
                        match.cases().collect(this::optimiseCase));
            } else {
                return new Match(
                    match.type(),
                    optimiseValue(match.scrutinee()),
                    match.cases().collect(this::optimiseCase));
            }
        } else if (expr instanceof UnOp unOp) {
            var result = analyseExpression(unOp.operand());
            if (result instanceof Constant constant) {
                return constant.value();
            } else {
                return new UnOp(unOp.type(), unOp.operator(), optimiseValue(unOp.operand()));
            }
        } else if (expr instanceof Value value) {
            return optimiseValue(value);
        }

        return expr;
    }

    ImmutableList<LocalBinding> optimiseBinding(LocalBinding binding) {
        if (environment.get(binding.name()) instanceof Constant) {
            // This binding is constant, so it can be removed and inlined
            return Lists.immutable.empty();
        } else if (environment.get(binding.name()) instanceof ConstantConstructor && funParams.containsKey(binding.name())) {
            // This binding is a constant function, so it can be removed and inlined
            return Lists.immutable.empty();
        } else if (binding instanceof Join join) {
            return Lists.immutable.of(
                new Join(join.name(), join.type(), join.params(), optimiseExpression(join.body())));
        } else if (binding instanceof LetAssign let) {
            return Lists.immutable.of(
                new LetAssign(let.name(), let.type(), optimiseExpression(let.body())));
        }

        return Lists.immutable.of(binding);
    }

    Case optimiseCase(Case cse) {
        return new Case(cse.pattern(), optimiseExpression(cse.consequent()));
    }

    Value optimiseValue(Value value) {
        if (value instanceof Box box) {
            return new Box(optimiseValue(box.value()));
        } else if (value instanceof Unbox unbox) {
            return new Unbox(optimiseValue(unbox.value()));
        } else if (value instanceof Lambda lambda) {
            return new Lambda(lambda.type(), lambda.params(), optimiseExpression(lambda.body()));
        } else if (value instanceof Literal literal) {
            return literal;
        } else if (value instanceof Reference reference) {
            var result = environment.get(reference.name());
            if (result instanceof Constant constant) {
                return constant.value();
            } else {
                return reference;
            }
        }

        return value;
    }

    public void analyseDeclarations(ImmutableList<Declaration> declarations) {
        var dataDecls = declarations.select(d -> d instanceof Data);
        var letDecls = declarations.select(d -> d instanceof Let);

        dataDecls.forEach(dataDecl -> {
            var data = (Data) dataDecl;
            data.constructors().forEach(constr -> {
                if (constr.fields().isEmpty()) {
                    putResult(constr.name(), new ConstantConstructor(constr.name()));
                } else {
                    putResult(constr.name(), new KnownConstructor(constr.name()));
                }
            });
        });

        letDecls.forEach(funDecl -> {
            var let = (Let) funDecl;

            // Use free variables within the declaration to build occurrence info
            var freeVars = funDecl.accept(freeVarsFolder);
            freeVars.forEach(freeVar -> occurrences.put(freeVar, funDecl.name()));

            // Save declaration bodies so we can revisit them while processing
            if (let.body() instanceof Lambda lambda) {
                funParams.put(let.name(), lambda.params());
                letBodies.put(let.name(), lambda.body());
                lambda.params().forEach(param -> {
                    // Assume that top-level functions can be called with any arguments
                    putResult(param.name(), NonConstant.VALUE);
                });
            } else {
                letBodies.put(let.name(), let.body());
            }

            // Initialise the worklist
            worklist.add(let.name());
        });

        Named funName;
        while ((funName = worklist.poll()) != null) {
            var initialResult = environment.getOrDefault(funName, Unknown.VALUE);
            var newResult = putResult(funName, analyseExpression(letBodies.get(funName)));
            if (newResult.compare(initialResult) > 0.0) {
                // If we gained more information about this function's
                // result, add its callsites to the worklist
                var funOccurrences = occurrences.get(funName);
                worklist.addAll(funOccurrences);
            }
        }
    }

    Type getUnderlyingType(Type type) {
        while (type instanceof QuantifiedType quant) {
            type = quant.body();
        }
        return type;
    }

    Result analyseExpression(Expression expr) {
        if (expr instanceof If ifExpr) {
            var condValue = analyseExpression(ifExpr.condition());
            if (condValue == Unknown.VALUE) {
                return condValue;
            } else if (condValue instanceof Constant constant &&
                constant.value() instanceof Boolean bool) {
                return bool.value()
                    ? analyseExpression(ifExpr.consequent())
                    : analyseExpression(ifExpr.alternative());
            } else {
                var consequent = analyseExpression(ifExpr.consequent());
                var alternative = analyseExpression(ifExpr.alternative());
                return Result.leastUpperBound(consequent, alternative);
            }
        } else if (expr instanceof Match match) {
            var scrutineeValue = analyseExpression(match.scrutinee());
            if (scrutineeValue == Unknown.VALUE) {
                return scrutineeValue;
            } else if (scrutineeValue instanceof Constant constant) {
                var matchingCase = matchingCase(match.cases(), constant);
                return matchingCase != null
                    ? analyseExpression(matchingCase.consequent())
                    : analyseCases(match.cases());
            } else if (scrutineeValue instanceof ConstructorResult known) {
                var matchingCases = matchingCases(match.cases(), known);
                return matchingCases.size() == 1
                    ? analyseExpression(matchingCases.getFirst().consequent())
                    : analyseCases(matchingCases);
            } else {
                return analyseCases(match.cases());
            }
        } else if (expr instanceof Block block) {
            Result result;
            var previousState = envState;

            while (true) {
                result = analyseExpression(block.result());

                for (var localBinding : block.bindings()) {
                    if (localBinding instanceof Join join) {
                        funParams.computeIfAbsent(join.name(), (k) -> join.params());
                        putResult(localBinding.name(), analyseExpression(join.body()));
                    } else if (localBinding.body() instanceof Lambda lambda) {
                        funParams.computeIfAbsent(localBinding.name(), (k) -> lambda.params());
                        putResult(localBinding.name(), analyseExpression(lambda.body()));
                    } else {
                        putResult(localBinding.name(), analyseExpression(localBinding.body()));
                    }
                }

                // Iterate until we stop finding new info
                if (envState > previousState) {
                    previousState = envState;
                } else {
                    break;
                }
            }

            return result;
        } else if (expr instanceof Lambda lambda) {
            // Pessimistically assume that we can't know how this lambda will be called
            lambda.params().forEach(param -> putResult(param.name(), NonConstant.VALUE));
            analyseExpression(lambda.body());
            return NonConstant.VALUE;
        } else if (expr instanceof Apply apply) {
            if (apply.expr() instanceof Lambda lambda) {
                // This is a lambda that is immediately applied
                analyseFunParams(lambda.params(), apply.args());
                return analyseExpression(lambda.body());
            } else if (apply.expr() instanceof Reference ref && funParams.containsKey(ref.name())) {
                // This is a known function
                putResult(ref.name(), Unknown.VALUE);

                var initialEnvState = envState;
                analyseFunParams(funParams.get(ref.name()), apply.args());
                // If we gained more information about a top-level function's
                // parameters, add the function to the worklist
                if (envState > initialEnvState && letBodies.containsKey(ref.name())) {
                    worklist.add(ref.name());
                }

                // Return the environment's mapping for this function
                return environment.get(ref.name());
            } else if (apply.expr() instanceof Reference ref) {
                // A constructor or a function from another namespace
                return environment.getOrDefault(ref.name(), Unknown.VALUE);
            } else {
                // We don't know what this is, but we can look at the argument
                // expressions to find information about other variables
                apply.args().forEach(this::analyseExpression);
                return analyseExpression(apply.expr());
            }
        } else if (expr instanceof BinOp binOp) {
            return analyseBinOp(binOp);
        } else if (expr instanceof UnOp unOp) {
            return analyseUnOp(unOp);
        } else if (expr instanceof Box box) {
            return analyseExpression(box.value());
        } else if (expr instanceof Unbox unbox) {
            return analyseExpression(unbox.value());
        } else if (expr instanceof Reference reference) {
            return Type.isFunction(getUnderlyingType(reference.type()))
                ? NonConstant.VALUE // Make no assumptions about functions passed as values
                : environment.getOrDefault(reference.name(), Unknown.VALUE);
        } else if (expr instanceof Literal literal) {
            return new Constant(literal);
        }

        // If in doubt, assume non-constant
        return NonConstant.VALUE;
    }

    void analyseFunParams(ImmutableList<Param> appliedParams, ImmutableList<Value> appliedArgs) {
        for (var pair : appliedParams.zip(appliedArgs)) {
            var argValue = analyseExpression(pair.getTwo());
            putResult(pair.getOne().name(), argValue);
        }
    }

    Result analysePattern(Pattern pattern) {
        if (pattern instanceof ConstructorPattern constr) {
            constr.fields().forEach(field -> analysePattern(field.pattern()));
            return new KnownConstructor(constr.name());
        } else if (pattern instanceof AliasPattern alias) {
            return putResult(alias.alias(), analysePattern(alias.pattern()));
        } else if (pattern instanceof LiteralPattern literal) {;
            return new Constant(literal.literal());
        } else if (pattern instanceof IdPattern id) {
            return putResult(id.name(), Unknown.VALUE);
        }

        return NonConstant.VALUE;
    }

    Case matchingCase(ImmutableList<Case> cases, Constant constant) {
        return cases.detect(cse -> {
            var result = analysePattern(cse.pattern());
            var isMatchingConstant = constant.equals(result);
            var isUnknown = result == Unknown.VALUE;
            return isMatchingConstant || isUnknown;
        });
    }

    ImmutableList<Case> matchingCases(ImmutableList<Case> cases, ConstructorResult known) {
        return cases.select(cse -> {
            var result = analysePattern(cse.pattern());
            var isMatchingConstructor = result instanceof ConstructorResult constr
                && known.constructor().equals(constr.constructor());
            var isUnknown = result == Unknown.VALUE;
            return isMatchingConstructor || isUnknown;
        });
    }

    Result analyseCases(ImmutableList<Case> cases) {
        return cases.collect(cse -> {
            analysePattern(cse.pattern());
            return analyseExpression(cse.consequent());
        }).stream().reduce(Unknown.VALUE, Result::leastUpperBound);
    }

    Result analyseUnOp(UnOp unOp) {
        var operand = analyseExpression(unOp.operand());
        if (operand instanceof Constant constant) {
           return switch (unOp.operator()) {
               case BOOLEAN_NOT -> {
                   if (constant.value() instanceof Boolean bool) {
                       yield new Constant(new Boolean(!bool.value()));
                   } else {
                       // Unexpected operand type
                       yield NonConstant.VALUE;
                   }
               }
               case BITWISE_NOT -> {
                   if (constant.value() instanceof Int intgr) {
                       yield new Constant(new Int(~intgr.value()));
                   } else if (constant.value() instanceof Long lng) {
                       yield new Constant(new Long(~lng.value()));
                   } else {
                       // Unexpected operand type
                       yield NonConstant.VALUE;
                   }
               }
               case NEGATE -> {
                   if (constant.value() instanceof Int intgr) {
                       yield new Constant(new Int(-intgr.value()));
                   } else if (constant.value() instanceof Long lng) {
                       yield new Constant(new Long(-lng.value()));
                   } else if (constant.value() instanceof Float flt) {
                       yield new Constant(new Float(-flt.value()));
                   } else if (constant.value() instanceof Double dbl) {
                       yield new Constant(new Double(-dbl.value()));
                   } else {
                       // Unexpected operand type
                       yield NonConstant.VALUE;
                   }
               }
           };
        } else {
            return operand;
        }
    }

    Result analyseBinOp(BinOp binOp) {
        var leftOperand = analyseExpression(binOp.left());
        var rightOperand = analyseExpression(binOp.right());
        if (leftOperand instanceof Constant left) {
            if ( // false && ...
                left.value() instanceof Boolean bool && !bool.value() && binOp.operator().equals(BinaryOp.BOOLEAN_AND)) {
                return leftOperand;
            } else if ( // true || ...
                left.value() instanceof Boolean bool && bool.value() && binOp.operator().equals(BinaryOp.BOOLEAN_OR)) {
                return leftOperand;
            } else if (rightOperand instanceof Constant right) {
                return evaluateBinOp(binOp, left, right);
            } else {
                return rightOperand;
            }
        } else if (rightOperand instanceof Constant) {
            return leftOperand;
        } else {
            return Result.leastUpperBound(leftOperand, rightOperand);
        }
    }

    Result evaluateBinOp(BinOp binOp, Constant left, Constant right) {
        return switch (binOp.operator()) {
            case MULTIPLY -> {
                if (left.value() instanceof Int l && right.value() instanceof Int r) {
                    yield new Constant(new Int(l.value() * r.value()));
                } else if (left.value() instanceof Long l && right.value() instanceof Long r) {
                    yield new Constant(new Long(l.value() * r.value()));
                } else if (left.value() instanceof Float l && right.value() instanceof Float r) {
                    yield new Constant(new Float(l.value() * r.value()));
                } else if (left.value() instanceof Double l && right.value() instanceof Double r) {
                    yield new Constant(new Double(l.value() * r.value()));
                } else {
                    // Unexpected operand types
                    throw new IllegalStateException("Mismatched operand types in binary operation: " + binOp);
                }
            }
            case DIVIDE -> {
                try {
                    if (left.value() instanceof Int l && right.value() instanceof Int r) {
                        yield new Constant(new Int(l.value() / r.value()));
                    } else if (left.value() instanceof Long l && right.value() instanceof Long r) {
                        yield new Constant(new Long(l.value() / r.value()));
                    } else if (left.value() instanceof Float l && right.value() instanceof Float r) {
                        yield new Constant(new Float(l.value() / r.value()));
                    } else if (left.value() instanceof Double l && right.value() instanceof Double r) {
                        yield new Constant(new Double(l.value() / r.value()));
                    } else {
                        // Unexpected operand types
                        throw new IllegalStateException("Mismatched operand types in binary operation: " + binOp);
                    }
                } catch (ArithmeticException e) {
                    yield Unknown.VALUE;
                }
            }
            case MODULUS -> {
                try {
                    if (left.value() instanceof Int l && right.value() instanceof Int r) {
                        yield new Constant(new Int(l.value() % r.value()));
                    } else if (left.value() instanceof Long l && right.value() instanceof Long r) {
                        yield new Constant(new Long(l.value() % r.value()));
                    } else if (left.value() instanceof Float l && right.value() instanceof Float r) {
                        yield new Constant(new Float(l.value() % r.value()));
                    } else if (left.value() instanceof Double l && right.value() instanceof Double r) {
                        yield new Constant(new Double(l.value() % r.value()));
                    } else {
                        // Unexpected operand types
                        throw new IllegalStateException("Mismatched operand types in binary operation: " + binOp);
                    }
                } catch (ArithmeticException e) {
                    yield Unknown.VALUE;
                }
            }
            case ADD -> {
                if (left.value() instanceof Int l && right.value() instanceof Int r) {
                    yield new Constant(new Int(l.value() + r.value()));
                } else if (left.value() instanceof Long l && right.value() instanceof Long r) {
                    yield new Constant(new Long(l.value() + r.value()));
                } else if (left.value() instanceof Float l && right.value() instanceof Float r) {
                    yield new Constant(new Float(l.value() + r.value()));
                } else if (left.value() instanceof Double l && right.value() instanceof Double r) {
                    yield new Constant(new Double(l.value() + r.value()));
                } else {
                    // Unexpected operand types
                    throw new IllegalStateException("Mismatched operand types in binary operation: " + binOp);
                }
            }
            case SUBTRACT -> {
                if (left.value() instanceof Int l && right.value() instanceof Int r) {
                    yield new Constant(new Int(l.value() - r.value()));
                } else if (left.value() instanceof Long l && right.value() instanceof Long r) {
                    yield new Constant(new Long(l.value() - r.value()));
                } else if (left.value() instanceof Float l && right.value() instanceof Float r) {
                    yield new Constant(new Float(l.value() - r.value()));
                } else if (left.value() instanceof Double l && right.value() instanceof Double r) {
                    yield new Constant(new Double(l.value() - r.value()));
                } else {
                    // Unexpected operand types
                    throw new IllegalStateException("Mismatched operand types in binary operation: " + binOp);
                }
            }
            case SHIFT_LEFT -> {
                if (left.value() instanceof Int l && right.value() instanceof Int r) {
                    yield new Constant(new Int(l.value() << r.value()));
                } else if (left.value() instanceof Long l && right.value() instanceof Int r) {
                    yield new Constant(new Long(l.value() << r.value()));
                } else {
                    // Unexpected operand types
                    throw new IllegalStateException("Mismatched operand types in binary operation: " + binOp);
                }
            }
            case SHIFT_RIGHT -> {
                if (left.value() instanceof Int l && right.value() instanceof Int r) {
                    yield new Constant(new Int(l.value() >> r.value()));
                } else if (left.value() instanceof Long l && right.value() instanceof Int r) {
                    yield new Constant(new Long(l.value() >> r.value()));
                } else {
                    // Unexpected operand types
                    throw new IllegalStateException("Mismatched operand types in binary operation: " + binOp);
                }
            }
            case UNSIGNED_SHIFT_RIGHT -> {
                if (left.value() instanceof Int l && right.value() instanceof Int r) {
                    yield new Constant(new Int(l.value() >>> r.value()));
                } else if (left.value() instanceof Long l && right.value() instanceof Int r) {
                    yield new Constant(new Long(l.value() >>> r.value()));
                } else {
                    // Unexpected operand types
                    throw new IllegalStateException("Mismatched operand types in binary operation: " + binOp);
                }
            }
            case BITWISE_AND -> {
                if (left.value() instanceof Int l && right.value() instanceof Int r) {
                    yield new Constant(new Int(l.value() & r.value()));
                } else if (left.value() instanceof Long l && right.value() instanceof Long r) {
                    yield new Constant(new Long(l.value() & r.value()));
                } else if (left.value() instanceof Boolean l && right.value() instanceof Boolean r) {
                    yield new Constant(new Boolean(l.value() & r.value()));
                } else {
                    // Unexpected operand types
                    throw new IllegalStateException("Mismatched operand types in binary operation: " + binOp);
                }
            }
            case BITWISE_OR -> {
                if (left.value() instanceof Int l && right.value() instanceof Int r) {
                    yield new Constant(new Int(l.value() | r.value()));
                } else if (left.value() instanceof Long l && right.value() instanceof Long r) {
                    yield new Constant(new Long(l.value() | r.value()));
                } else if (left.value() instanceof Boolean l && right.value() instanceof Boolean r) {
                    yield new Constant(new Boolean(l.value() | r.value()));
                } else {
                    // Unexpected operand types
                    throw new IllegalStateException("Mismatched operand types in binary operation: " + binOp);
                }
            }
            case BITWISE_XOR -> {
                if (left.value() instanceof Int l && right.value() instanceof Int r) {
                    yield new Constant(new Int(l.value() ^ r.value()));
                } else if (left.value() instanceof Long l && right.value() instanceof Long r) {
                    yield new Constant(new Long(l.value() ^ r.value()));
                } else if (left.value() instanceof Boolean l && right.value() instanceof Boolean r) {
                    yield new Constant(new Boolean(l.value() ^ r.value()));
                } else {
                    // Unexpected operand types
                    throw new IllegalStateException("Mismatched operand types in binary operation: " + binOp);
                }
            }
            case LESS_THAN -> {
                if (left.value() instanceof Int l && right.value() instanceof Int r) {
                    yield new Constant(new Boolean(l.value() < r.value()));
                } else if (left.value() instanceof Long l && right.value() instanceof Long r) {
                    yield new Constant(new Boolean(l.value() < r.value()));
                } else if (left.value() instanceof Float l && right.value() instanceof Float r) {
                    yield new Constant(new Boolean(l.value() < r.value()));
                } else if (left.value() instanceof Double l && right.value() instanceof Double r) {
                    yield new Constant(new Boolean(l.value() < r.value()));
                } else {
                    // Unexpected operand types
                    throw new IllegalStateException("Mismatched operand types in binary operation: " + binOp);
                }
            }
            case LESS_THAN_EQUAL -> {
                if (left.value() instanceof Int l && right.value() instanceof Int r) {
                    yield new Constant(new Boolean(l.value() <= r.value()));
                } else if (left.value() instanceof Long l && right.value() instanceof Long r) {
                    yield new Constant(new Boolean(l.value() <= r.value()));
                } else if (left.value() instanceof Float l && right.value() instanceof Float r) {
                    yield new Constant(new Boolean(l.value() <= r.value()));
                } else if (left.value() instanceof Double l && right.value() instanceof Double r) {
                    yield new Constant(new Boolean(l.value() <= r.value()));
                } else {
                    // Unexpected operand types
                    throw new IllegalStateException("Mismatched operand types in binary operation: " + binOp);
                }
            }
            case GREATER_THAN -> {
                if (left.value() instanceof Int l && right.value() instanceof Int r) {
                    yield new Constant(new Boolean(l.value() > r.value()));
                } else if (left.value() instanceof Long l && right.value() instanceof Long r) {
                    yield new Constant(new Boolean(l.value() > r.value()));
                } else if (left.value() instanceof Float l && right.value() instanceof Float r) {
                    yield new Constant(new Boolean(l.value() > r.value()));
                } else if (left.value() instanceof Double l && right.value() instanceof Double r) {
                    yield new Constant(new Boolean(l.value() > r.value()));
                } else {
                    // Unexpected operand types
                    throw new IllegalStateException("Mismatched operand types in binary operation: " + binOp);
                }
            }
            case GREATER_THAN_EQUAL -> {
                if (left.value() instanceof Int l && right.value() instanceof Int r) {
                    yield new Constant(new Boolean(l.value() >= r.value()));
                } else if (left.value() instanceof Long l && right.value() instanceof Long r) {
                    yield new Constant(new Boolean(l.value() >= r.value()));
                } else if (left.value() instanceof Float l && right.value() instanceof Float r) {
                    yield new Constant(new Boolean(l.value() >= r.value()));
                } else if (left.value() instanceof Double l && right.value() instanceof Double r) {
                    yield new Constant(new Boolean(l.value() >= r.value()));
                } else {
                    // Unexpected operand types
                    throw new IllegalStateException("Mismatched operand types in binary operation: " + binOp);
                }
            }
            case BOOLEAN_AND -> {
                if (left.value() instanceof Boolean l && right.value() instanceof Boolean r) {
                    yield new Constant(new Boolean(l.value() && r.value()));
                } else {
                    // Unexpected operand types
                    throw new IllegalStateException("Mismatched operand types in binary operation: " + binOp);
                }
            }
            case BOOLEAN_OR -> {
                if (left.value() instanceof Boolean l && right.value() instanceof Boolean r) {
                    yield new Constant(new Boolean(l.value() || r.value()));
                } else {
                    // Unexpected operand types
                    throw new IllegalStateException("Mismatched operand types in binary operation: " + binOp);
                }
            }
            case EQUAL -> {
                yield new Constant(new Boolean(left.equals(right)));
            }
            case NOT_EQUAL -> {
                yield new Constant(new Boolean(!left.equals(right)));
            }
        };
    }
}

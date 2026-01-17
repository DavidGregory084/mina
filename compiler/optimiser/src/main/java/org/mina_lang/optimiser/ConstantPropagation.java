/*
 * SPDX-FileCopyrightText:  © 2026 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.optimiser;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.multimap.set.MutableSetMultimap;
import org.eclipse.collections.impl.factory.Multimaps;
import org.mina_lang.common.names.Named;
import org.mina_lang.common.operators.BinaryOp;
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

    ConstantPropagation(Map<Named, Result> environment) {
        this.environment = environment;
        this.worklist = new ArrayDeque<>();
        this.letBodies = new HashMap<>();
        this.occurrences = Multimaps.mutable.set.empty();
        this.funParams = new HashMap<>();
        this.freeVarsFolder = new FreeVariablesFolder();
        this.envState = 0L;
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

    public void analyseDeclarations(ImmutableList<Declaration> declarations) {
        var dataDecls = declarations.select(d -> d instanceof Data);
        var letDecls = declarations.select(d -> d instanceof Let);

        dataDecls.forEach(dataDecl -> {
            var data = (Data) dataDecl;
            data.constructors().forEach(constr -> {
                putResult(constr.name(), new KnownConstructor(constr.name()));
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
            var initialResult = environment.getOrDefault(funName, Unassigned.VALUE);
            var newResult = putResult(funName, analyseExpression(letBodies.get(funName)));
            if (newResult.compare(initialResult) > 0.0) {
                // If we gained more information about this function's
                // result, add its callsites to the worklist
                var funOccurrences = occurrences.get(funName);
                worklist.addAll(funOccurrences);
            }
        }
    }

    Result analyseExpression(Expression expr) {
        if (expr instanceof If ifExpr) {
            var condValue = analyseExpression(ifExpr.condition());
            if (condValue == Unassigned.VALUE) {
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
            if (scrutineeValue == Unassigned.VALUE) {
                return scrutineeValue;
            } else {
                return match.cases()
                    .collect(cse -> {
                        analysePattern(cse.pattern());
                        return analyseExpression(cse.consequent());
                    })
                    .stream().reduce(Unassigned.VALUE, Result::leastUpperBound);
            }
        } else if (expr instanceof Block block) {
            for (var localBinding : block.bindings()) {
                if (localBinding instanceof Join join) {
                    putResult(localBinding.name(), analyseExpression(join.body()));
                } else if (localBinding.body() instanceof Lambda lambda) {
                    putResult(localBinding.name(), analyseExpression(lambda.body()));
                } else {
                    putResult(localBinding.name(), analyseExpression(localBinding.body()));
                }
            }
            return analyseExpression(block.result());
        } else if (expr instanceof Lambda lambda) {
            // Pessimistically assume that we can't know how this lambda will be called
            lambda.params().forEach(param -> putResult(param.name(), NonConstant.VALUE));
            analyseExpression(lambda.body());
            return NonConstant.VALUE;
        } else if (expr instanceof Apply apply) {
            analyseExpression(apply.expr());

            if (apply.expr() instanceof Lambda) {
                // We are applying an unknown lambda
                return NonConstant.VALUE;
            } else if (apply.expr() instanceof Reference ref && funParams.containsKey(ref.name())) {
                // This is a known function
                putResult(ref.name(), Unassigned.VALUE);

                var initialEnvState = envState;
                analyseFunParams(funParams.get(ref.name()), apply.args());
                // If we gained more information about this function's
                // parameters, add the function to the worklist
                if (envState > initialEnvState) {
                    worklist.add(ref.name());
                }

                // Return the environment's mapping for this function
                return environment.get(ref.name());
            } else if (apply.expr() instanceof Reference ref) {
                // A constructor or a function from another namespace
                return environment.getOrDefault(ref.name(), Unassigned.VALUE);
            } else {
                // We don't know what this is, but we can look at the argument
                // expressions to find information about other variables
                apply.args().forEach(this::analyseExpression);
                return Unassigned.VALUE;
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
            return environment.getOrDefault(reference.name(), Unassigned.VALUE);
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
            return putResult(id.name(), Unassigned.VALUE);
        }

        return NonConstant.VALUE;
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
                return evaluateBinOp(binOp.operator(), left, right);
            } else {
                return rightOperand;
            }
        } else if (rightOperand instanceof Constant) {
            return leftOperand;
        } else {
            return Result.leastUpperBound(leftOperand, rightOperand);
        }
    }

    Result evaluateBinOp(BinaryOp operator, Constant left, Constant right) {
        return switch (operator) {
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
                    yield NonConstant.VALUE;
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
                        yield NonConstant.VALUE;
                    }
                } catch (ArithmeticException e) {
                    yield Unassigned.VALUE;
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
                        yield NonConstant.VALUE;
                    }
                } catch (ArithmeticException e) {
                    yield Unassigned.VALUE;
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
                    yield NonConstant.VALUE;
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
                    yield NonConstant.VALUE;
                }
            }
            case SHIFT_LEFT -> {
                if (left.value() instanceof Int l && right.value() instanceof Int r) {
                    yield new Constant(new Int(l.value() << r.value()));
                } else if (left.value() instanceof Long l && right.value() instanceof Int r) {
                    yield new Constant(new Long(l.value() << r.value()));
                } else {
                    // Unexpected operand types
                    yield NonConstant.VALUE;
                }
            }
            case SHIFT_RIGHT -> {
                if (left.value() instanceof Int l && right.value() instanceof Int r) {
                    yield new Constant(new Int(l.value() >> r.value()));
                } else if (left.value() instanceof Long l && right.value() instanceof Int r) {
                    yield new Constant(new Long(l.value() >> r.value()));
                } else {
                    // Unexpected operand types
                    yield NonConstant.VALUE;
                }
            }
            case UNSIGNED_SHIFT_RIGHT -> {
                if (left.value() instanceof Int l && right.value() instanceof Int r) {
                    yield new Constant(new Int(l.value() >>> r.value()));
                } else if (left.value() instanceof Long l && right.value() instanceof Int r) {
                    yield new Constant(new Long(l.value() >>> r.value()));
                } else {
                    // Unexpected operand types
                    yield NonConstant.VALUE;
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
                    yield NonConstant.VALUE;
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
                    yield NonConstant.VALUE;
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
                    yield NonConstant.VALUE;
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
                    yield NonConstant.VALUE;
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
                    yield NonConstant.VALUE;
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
                    yield NonConstant.VALUE;
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
                    yield NonConstant.VALUE;
                }
            }
            case BOOLEAN_AND -> {
                if (left.value() instanceof Boolean l && right.value() instanceof Boolean r) {
                    yield new Constant(new Boolean(l.value() && r.value()));
                } else {
                    // Unexpected operand types
                    yield NonConstant.VALUE;
                }
            }
            case BOOLEAN_OR -> {
                if (left.value() instanceof Boolean l && right.value() instanceof Boolean r) {
                    yield new Constant(new Boolean(l.value() || r.value()));
                } else {
                    // Unexpected operand types
                    yield NonConstant.VALUE;
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

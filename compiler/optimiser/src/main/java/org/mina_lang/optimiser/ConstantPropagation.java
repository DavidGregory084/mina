package org.mina_lang.optimiser;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.names.Named;
import org.mina_lang.common.operators.BinaryOp;
import org.mina_lang.ina.*;
import org.mina_lang.ina.Boolean;
import org.mina_lang.ina.Double;
import org.mina_lang.ina.Float;
import org.mina_lang.ina.Long;
import org.mina_lang.optimiser.constants.Constant;
import org.mina_lang.optimiser.constants.NonConstant;
import org.mina_lang.optimiser.constants.Result;
import org.mina_lang.optimiser.constants.Unassigned;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class ConstantPropagation {
    private final Map<Named, Result> environment;
    private final Queue<Named> worklist;
    private final Map<Named, Expression> letBodies;
    private final Map<Named, ImmutableList<Named>> occurrences;
    private final Map<Named, ImmutableList<Param>> funParams;

    ConstantPropagation(Map<Named, Result> environment) {
        this.environment = environment;
        this.worklist = new ArrayDeque<>();
        this.letBodies = new HashMap<>();
        this.occurrences = new HashMap<>();
        this.funParams = new HashMap<>();
    }

    public ConstantPropagation() {
        this(new HashMap<>());
    }

    private void putResult(Named name, Result newValue) {
        environment.compute(name, (n, existingValue) -> {
            return (existingValue != null)
                ? Result.leastUpperBound(existingValue, newValue)
                : newValue;
        });
    }

    void analyseDeclarations(ImmutableList<Declaration> declarations) {
        var funDecls = declarations.select(d -> d instanceof Let let);

        // Initialise the worklist
        funDecls.forEach(funDecl -> {
            var let = (Let) funDecl;
            if (let.body() instanceof Lambda lambda) {
                funParams.put(let.name(), lambda.params());
                letBodies.put(let.name(), lambda.body());
            } else {
                letBodies.put(let.name(), let.body());
            }
            worklist.add(let.name());
        });

        Named funName;
        while ((funName = worklist.poll()) != null) {

        }
    }

    Result analyseExpression(Expression expr) {
        if (expr instanceof If ifExpr) {
            var condValue = analyseExpression(ifExpr.condition());
            if (condValue == Unassigned.VALUE) {
                return condValue;
            } else if (condValue instanceof Constant constant &&
                constant.value() instanceof Boolean bool)  {
                return bool.value()
                    ? analyseExpression(ifExpr.consequent())
                    : analyseExpression(ifExpr.alternative());
            } else {
                var consequent = analyseExpression(ifExpr.consequent());
                var alternative = analyseExpression(ifExpr.alternative());
                return Result.leastUpperBound(consequent, alternative);
            }
        } else if (expr instanceof Block block) {
            for (var localBinding : block.bindings()) {
                if (localBinding instanceof Join join) {
                    funParams.put(localBinding.name(), join.params());
                    putResult(localBinding.name(), analyseExpression(join.body()));
                } else if (localBinding.body() instanceof Lambda lambda) {
                    funParams.put(localBinding.name(), lambda.params());
                    putResult(localBinding.name(), analyseExpression(lambda.body()));
                } else {
                    putResult(localBinding.name(), analyseExpression(localBinding.body()));
                }
            }
            return analyseExpression(block.result());
        } else if (expr instanceof Lambda lambda) {
            // Pessimistically assume that we don't know how this lambda will be called
            lambda.params().forEach(param -> putResult(param.name(), NonConstant.VALUE));
            analyseExpression(lambda.body());
            return NonConstant.VALUE;
        } else if (expr instanceof Apply apply) {
            ImmutableList<Param> appliedParams = Lists.immutable.empty();
            if (apply.expr() instanceof Reference ref && funParams.containsKey(ref.name())) {
                appliedParams = funParams.get(ref.name());
            } else if (apply.expr() instanceof Lambda lambda) {
                appliedParams = lambda.params();
            }
            for (var pair : appliedParams.zip(apply.args())) {
                var argValue = analyseExpression(pair.getTwo());
                putResult(pair.getOne().name(), argValue);
            }
        } else if (expr instanceof BinOp binOp) {
            return analyseBinOp(binOp);
        } else if (expr instanceof UnOp unOp) {
            return analyseUnOp(unOp);
        } else if (expr instanceof Reference reference) {
            return environment.getOrDefault(reference.name(), Unassigned.VALUE);
        } else if (expr instanceof Literal literal) {
            return new Constant(literal);
        }

        // If in doubt, assume non-constant
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
            if (rightOperand instanceof Constant right) {
                return evaluateBinOp(binOp.operator(), left, right);
            } else if ( // false && ...
               left.value() instanceof Boolean bool && !bool.value() && binOp.operator().equals(BinaryOp.BOOLEAN_AND)) {
                return leftOperand;
            } else if ( // true || ...
                left.value() instanceof Boolean bool && bool.value() && binOp.operator().equals(BinaryOp.BOOLEAN_OR)) {
                return leftOperand;
            } else {
                return rightOperand;
            }
        } else if (rightOperand instanceof Constant right) {
            if ( // ... && false
                right.value() instanceof Boolean bool && !bool.value() && binOp.operator().equals(BinaryOp.BOOLEAN_AND)) {
                return rightOperand;
            } else if ( // ... || true
                right.value() instanceof Boolean bool && bool.value() && binOp.operator().equals(BinaryOp.BOOLEAN_OR)) {
                return rightOperand;
            } else {
                return leftOperand;
            }
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

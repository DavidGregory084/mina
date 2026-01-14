package org.mina_lang.optimiser;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.names.Named;
import org.mina_lang.ina.*;
import org.mina_lang.ina.Boolean;
import org.mina_lang.optimiser.constants.Constant;
import org.mina_lang.optimiser.constants.NonConstant;
import org.mina_lang.optimiser.constants.Result;
import org.mina_lang.optimiser.constants.Unassigned;

import java.util.HashMap;
import java.util.Map;

public class ConstantPropagation {
    private final Map<Named, Result> environment = new HashMap<>();

    public ConstantPropagation() {

    }

    public Namespace propagate(Namespace namespace) {
        var name = namespace.name();
        var declarations = namespace.declarations();
        analyseDeclarations(declarations);
        var updatedDeclarations = declarations.collect(this::propagateDeclaration);
        return new Namespace(name, updatedDeclarations);
    }

    Declaration propagateDeclaration(Declaration declaration) {
        if (declaration instanceof Data data) {
            return data;
        } else if (declaration instanceof Let let) {
            return propagateLet(let);
        }

        return null;
    }

    Let propagateLet(Let let) {
        var result = analyseLet(let);
        if (result instanceof Constant constant) {
           return new Let(let.name(), let.type(), constant.value());
        } else {
            return let;
        }
    }

    void analyseDeclarations(ImmutableList<Declaration> declarations) {
        var previous = new HashMap<>(environment);
        do {
        } while (!environment.equals(previous));
    }

    Result analyseLet(Let let) {
        var bodyResult = analyseExpression(let.body());
        return null;
    }

    Result analyseExpression(Expression expr) {
        if (expr instanceof If ifExpr) {
            var condValue = analyseExpression(ifExpr.condition());
            if (condValue == Unassigned.VALUE) {
                return condValue;
            } if (condValue instanceof Constant constant &&
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
        } else if (expr instanceof BinOp binOp) {
        } else if (expr instanceof UnOp unOp) {
        } else if (expr instanceof Apply apply) {
        } else if (expr instanceof Lambda lambda) {
        } else if (expr instanceof Reference reference) {
            return environment.getOrDefault(reference.name(), Unassigned.VALUE);
        } else if (expr instanceof Literal literal) {
            return new Constant(literal);
        }

        // If in doubt, assume non-constant
        return NonConstant.VALUE;
    }
}

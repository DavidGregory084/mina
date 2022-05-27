package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record LambdaExprNode(ImmutableList<ParamNode> params, ExprNode body) implements ExprNode {
    
}

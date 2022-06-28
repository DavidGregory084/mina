package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record LambdaExprNode<A>(Meta<A> meta, ImmutableList<ParamNode<A>> params, ExprNode<A> body) implements ExprNode<A> {

}
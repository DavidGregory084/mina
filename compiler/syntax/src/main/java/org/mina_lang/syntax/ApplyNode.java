package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record ApplyNode<A> (Meta<A> meta, ExprNode<A> expr, ImmutableList<ExprNode<A>> args) implements ExprNode<A> {

}

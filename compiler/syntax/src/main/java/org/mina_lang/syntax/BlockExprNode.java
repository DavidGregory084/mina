package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record BlockExprNode<A>(Meta<A> meta, ImmutableList<LetDeclarationNode<A>> declarations, ExprNode<A> result) implements ExprNode<A> {

}

package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record MatchNode<A>(Meta<A> meta, ExprNode<A> scrutinee, ImmutableList<CaseNode<A>> cases) implements ExprNode<A> {

}

package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record TypeApplyNode<A> (Meta<A> meta, TypeNode<A> type, ImmutableList<TypeNode<A>> args)
        implements TypeNode<A> {

}

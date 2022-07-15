package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record TypeLambdaNode<A> (Meta<A> meta, ImmutableList<TypeParamNode<A>> args, TypeNode<A> body)
        implements TypeNode<A> {

}

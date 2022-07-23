package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

import java.util.Optional;

public record ConstructorNode<A>(Meta<A> meta, String name, ImmutableList<ConstructorParamNode<A>> params, Optional<TypeNode<Void>> type) implements SyntaxNode<A> {

}

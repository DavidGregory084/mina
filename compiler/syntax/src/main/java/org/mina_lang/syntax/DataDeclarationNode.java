package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record DataDeclarationNode<A>(Meta<A> meta, String name, ImmutableList<TypeVarNode<Void>> typeParams, ImmutableList<ConstructorNode<A>> constructors) implements DeclarationNode<A> {

}

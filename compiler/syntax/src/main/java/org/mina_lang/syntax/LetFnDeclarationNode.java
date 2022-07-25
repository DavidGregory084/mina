package org.mina_lang.syntax;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;

public record LetFnDeclarationNode<A>(Meta<A> meta, String name, ImmutableList<TypeVarNode<Void>> typeParams, ImmutableList<ParamNode<A>> valueParams, Optional<TypeNode<Void>> returnType, ExprNode<A> expr) implements DeclarationNode<A> {

}

package org.mina_lang.syntax;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;

public record LetFnNode<A>(Meta<A> meta, String name, ImmutableList<TypeVarNode<A>> typeParams, ImmutableList<ParamNode<A>> valueParams, Optional<TypeNode<A>> returnType, ExprNode<A> expr) implements DeclarationNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        // Visit types
        typeParams.forEach(tyParam -> tyParam.accept(visitor));
        returnType.ifPresent(returnTy -> returnTy.accept(visitor));
        // Visit values
        valueParams.forEach(param -> param.accept(visitor));
        expr.accept(visitor);

        visitor.visitLetFn(this);
    }
}

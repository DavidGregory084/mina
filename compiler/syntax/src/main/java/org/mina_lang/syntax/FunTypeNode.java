package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record FunTypeNode<A>(Meta<A> meta, ImmutableList<TypeNode<A>> argTypes, TypeNode<A> returnType) implements TypeNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        argTypes.forEach(argTy -> argTy.accept(visitor));
        returnType.accept(visitor);
        visitor.visitFunType(this);
    }
}

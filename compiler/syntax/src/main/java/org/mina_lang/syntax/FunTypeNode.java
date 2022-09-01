package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record FunTypeNode<A> (Meta<A> meta, ImmutableList<TypeNode<A>> argTypes, TypeNode<A> returnType)
        implements TypeNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        argTypes.forEach(argTy -> argTy.accept(visitor));
        returnType.accept(visitor);
        visitor.visitFunType(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitFunType(
                meta(),
                argTypes().collect(visitor::visitType),
                visitor.visitType(returnType()));
    }

    @Override
    public <B> FunTypeNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitFunType(
                meta(),
                argTypes().collect(transformer::visitType),
                transformer.visitType(returnType()));
    }
}

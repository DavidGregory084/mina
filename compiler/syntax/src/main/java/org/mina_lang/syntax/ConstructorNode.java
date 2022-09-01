package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

import java.util.Optional;

public record ConstructorNode<A> (Meta<A> meta, String name, ImmutableList<ConstructorParamNode<A>> params,
        Optional<TypeNode<A>> type) implements MetaNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        params.forEach(param -> param.accept(visitor));
        type.ifPresent(typ -> typ.accept(visitor));
        visitor.visitConstructor(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitConstructor(
                meta(),
                name(),
                params().collect(param -> param.accept(visitor)),
                type().map(visitor::visitType));
    }

    @Override
    public <B> ConstructorNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitConstructor(
                meta(),
                name(),
                params().collect(param -> param.accept(transformer)),
                type().map(transformer::visitType));
    }
}

package org.mina_lang.syntax;

import java.util.Optional;

import org.mina_lang.common.Meta;

public record ParamNode<A> (Meta<A> meta, String name, Optional<TypeNode<A>> typeAnnotation) implements MetaNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        typeAnnotation.ifPresent(tyAnn -> tyAnn.accept(visitor));
        visitor.visitParam(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitParam(
                meta(),
                name(),
                typeAnnotation().map(visitor::visitType));
    }

    @Override
    public <B> ParamNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitParam(
                meta(),
                name(),
                typeAnnotation().map(transformer::visitType));
    }
}

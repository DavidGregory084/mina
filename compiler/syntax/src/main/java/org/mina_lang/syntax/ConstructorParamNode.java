package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record ConstructorParamNode<A> (Meta<A> meta, String name, TypeNode<A> typeAnnotation)
        implements MetaNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        typeAnnotation.accept(visitor);
        visitor.visitConstructorParam(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitConstructorParam(
            meta(),
            name(),
            visitor.visitType(typeAnnotation()));
    }

    @Override
    public <B> ConstructorParamNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitConstructorParam(
            meta(),
            name(),
            transformer.visitType(typeAnnotation()));
    }
}

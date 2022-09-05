package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record DoubleNode<A> (Meta<A> meta, double value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitDouble(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitDouble(meta(), value());
    }

    @Override
    public <B> DoubleNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitDouble(meta(), value());
    }
}

package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record FloatNode<A> (Meta<A> meta, float value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitFloat(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitFloat(meta(), value());
    }

    @Override
    public <B> FloatNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitFloat(meta(), value());
    }
}

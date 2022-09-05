package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record LongNode<A>(Meta<A> meta, long value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitLong(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitLong(meta(), value());
    }

    @Override
    public <B> LongNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitLong(meta(), value());
    }
}

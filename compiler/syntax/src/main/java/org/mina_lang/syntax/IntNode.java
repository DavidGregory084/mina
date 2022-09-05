package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record IntNode<A>(Meta<A> meta, int value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitInt(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitInt(meta(), value());
    }

    @Override
    public <B> IntNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitInt(meta(), value());
    }
}

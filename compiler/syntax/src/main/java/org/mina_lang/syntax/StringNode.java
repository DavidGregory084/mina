package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record StringNode<A>(Meta<A> meta, String value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitString(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitString(meta(), value());
    }

    @Override
    public <B> StringNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitString(meta(), value());
    }
}

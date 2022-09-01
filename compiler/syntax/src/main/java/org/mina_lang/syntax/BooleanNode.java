package org.mina_lang.syntax;

public record BooleanNode<A> (Meta<A> meta, boolean value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitBoolean(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitBoolean(meta(), value());
    }

    @Override
    public <B> BooleanNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitBoolean(meta(), value());
    }
}

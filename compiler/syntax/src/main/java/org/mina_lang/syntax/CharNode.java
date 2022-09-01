package org.mina_lang.syntax;

public record CharNode<A> (Meta<A> meta, char value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitChar(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitChar(meta(), value());
    }

    @Override
    public <B> CharNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitChar(meta(), value());
    }
}

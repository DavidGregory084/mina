package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record CharNode<A> (Meta<A> meta, char value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitChar(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitChar(this);
        var result = visitor.visitChar(meta(), value());
        visitor.postVisitChar(result);
        return result;
    }

    @Override
    public <B> CharNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitChar(this);
        var result = visitor.visitChar(meta(), value());
        visitor.postVisitChar(result);
        return result;
    }
}

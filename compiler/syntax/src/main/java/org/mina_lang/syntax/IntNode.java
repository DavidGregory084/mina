package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record IntNode<A>(Meta<A> meta, int value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitInt(this);
    }

    @Override
    public <B> B accept(LiteralNodeFolder<A, B> visitor) {
        visitor.preVisitInt(this);
        var result = visitor.visitInt(meta(), value());
        visitor.postVisitInt(this);
        return result;
    }

    @Override
    public <B> IntNode<B> accept(LiteralNodeTransformer<A, B> visitor) {
        visitor.preVisitInt(this);
        var result = visitor.visitInt(meta(), value());
        visitor.postVisitInt(result);
        return result;
    }

    @Override
    public Integer boxedValue() {
        return value();
    }
}

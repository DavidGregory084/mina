package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record LongNode<A>(Meta<A> meta, long value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitLong(this);
    }

    @Override
    public <B> B accept(LiteralNodeFolder<A, B> visitor) {
        visitor.preVisitLong(this);
        var result = visitor.visitLong(meta(), value());
        visitor.postVisitLong(this);
        return result;
    }

    @Override
    public <B> LongNode<B> accept(LiteralNodeTransformer<A, B> visitor) {
        visitor.preVisitLong(this);
        var result = visitor.visitLong(meta(), value());
        visitor.postVisitLong(result);
        return result;
    }

    @Override
    public Long boxedValue() {
        return value();
    }
}

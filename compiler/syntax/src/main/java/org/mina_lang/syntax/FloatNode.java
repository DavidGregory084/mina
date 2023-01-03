package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record FloatNode<A> (Meta<A> meta, float value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitFloat(this);
    }

    @Override
    public <B> B accept(LiteralNodeFolder<A, B> visitor) {
        visitor.preVisitFloat(this);
        var result = visitor.visitFloat(meta(), value());
        visitor.postVisitFloat(this);
        return result;
    }

    @Override
    public <B> FloatNode<B> accept(LiteralNodeTransformer<A, B> visitor) {
        visitor.preVisitFloat(this);
        var result = visitor.visitFloat(meta(), value());
        visitor.postVisitFloat(result);
        return result;
    }

    @Override
    public Float boxedValue() {
        return value();
    }
}

package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record DoubleNode<A> (Meta<A> meta, double value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitDouble(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitDouble(this);
        var result = visitor.visitDouble(meta(), value());
        visitor.postVisitDouble(result);
        return result;
    }

    @Override
    public <B> DoubleNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitDouble(this);
        var result = visitor.visitDouble(meta(), value());
        visitor.postVisitDouble(result);
        return result;
    }
}

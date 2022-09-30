package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record FloatNode<A> (Meta<A> meta, float value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitFloat(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitFloat(this);
        var result = visitor.visitFloat(meta(), value());
        visitor.postVisitFloat(result);
        return result;
    }

    @Override
    public <B> FloatNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitFloat(this);
        var result = visitor.visitFloat(meta(), value());
        visitor.postVisitFloat(result);
        return result;
    }
}

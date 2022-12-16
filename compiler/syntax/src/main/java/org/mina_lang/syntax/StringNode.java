package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record StringNode<A>(Meta<A> meta, String value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitString(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitString(this);
        var result = visitor.visitString(meta(), value());
        visitor.preVisitString(this);
        return result;
    }

    @Override
    public <B> StringNode<B> accept(LiteralNodeTransformer<A, B> visitor) {
        visitor.preVisitString(this);
        var result = visitor.visitString(meta(), value());
        visitor.preVisitString(this);
        return result;
    }
}

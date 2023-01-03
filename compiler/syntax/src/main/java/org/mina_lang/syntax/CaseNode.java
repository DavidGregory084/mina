package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record CaseNode<A> (Meta<A> meta, PatternNode<A> pattern, ExprNode<A> consequent) implements MetaNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        pattern.accept(visitor);
        consequent.accept(visitor);
        visitor.visitCase(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitCase(this);

        var result = visitor.visitCase(
                meta(),
                visitor.visitPattern(pattern()),
                visitor.visitExpr(consequent()));

        visitor.postVisitCase(this);

        return result;
    }

    @Override
    public <B> CaseNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitCase(this);

        var result = visitor.visitCase(
                meta(),
                visitor.visitPattern(pattern()),
                visitor.visitExpr(consequent()));

        visitor.postVisitCase(result);

        return result;
    }
}

package org.mina_lang.syntax;

public record CaseNode<A> (Meta<A> meta, PatternNode<A> pattern, ExprNode<A> consequent) implements MetaNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        pattern.accept(visitor);
        consequent.accept(visitor);
        visitor.visitCase(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitCase(
                meta(),
                visitor.visitPattern(pattern()),
                visitor.visitExpr(consequent()));
    }

    @Override
    public <B> CaseNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitCase(
                meta(),
                transformer.visitPattern(pattern()),
                transformer.visitExpr(consequent()));
    }
}

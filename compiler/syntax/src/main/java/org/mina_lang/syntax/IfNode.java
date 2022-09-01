package org.mina_lang.syntax;

public record IfNode<A> (Meta<A> meta, ExprNode<A> condition, ExprNode<A> consequent, ExprNode<A> alternative)
        implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        condition.accept(visitor);
        consequent.accept(visitor);
        alternative.accept(visitor);
        visitor.visitIf(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitIf(
                meta(),
                visitor.visitExpr(condition()),
                visitor.visitExpr(alternative()),
                visitor.visitExpr(consequent()));
    }

    @Override
    public <B> IfNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitIf(
                meta(),
                transformer.visitExpr(condition()),
                transformer.visitExpr(alternative()),
                transformer.visitExpr(consequent()));
    }
}

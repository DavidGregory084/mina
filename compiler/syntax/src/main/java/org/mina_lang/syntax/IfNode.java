package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

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
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitIf(this);

        var result = visitor.visitIf(
                meta(),
                visitor.visitExpr(condition()),
                visitor.visitExpr(consequent()),
                visitor.visitExpr(alternative()));

        visitor.postVisitIf(result);

        return result;
    }

    @Override
    public <B> IfNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitIf(this);

        var result = visitor.visitIf(
                meta(),
                visitor.visitExpr(condition()),
                visitor.visitExpr(consequent()),
                visitor.visitExpr(alternative()));

        visitor.postVisitIf(result);

        return result;
    }
}

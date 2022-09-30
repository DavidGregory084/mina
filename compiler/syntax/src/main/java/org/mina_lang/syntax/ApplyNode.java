package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public record ApplyNode<A> (Meta<A> meta, ExprNode<A> expr, ImmutableList<ExprNode<A>> args) implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        expr.accept(visitor);
        args.forEach(arg -> arg.accept(visitor));
        visitor.visitApply(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitApply(this);

        var result = visitor.visitApply(
                meta(),
                visitor.visitExpr(expr()),
                args().collect(visitor::visitExpr));

        visitor.postVisitApply(result);

        return result;
    }

    @Override
    public <B> ApplyNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitApply(this);

        var result = visitor.visitApply(
                meta(),
                visitor.visitExpr(expr()),
                args().collect(visitor::visitExpr));

        visitor.postVisitApply(result);

        return result;
    }
}

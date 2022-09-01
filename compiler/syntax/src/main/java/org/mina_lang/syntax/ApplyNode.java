package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record ApplyNode<A> (Meta<A> meta, ExprNode<A> expr, ImmutableList<ExprNode<A>> args) implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        expr.accept(visitor);
        args.forEach(arg -> arg.accept(visitor));
        visitor.visitApply(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitApply(
                meta(),
                visitor.visitExpr(expr()),
                args().collect(visitor::visitExpr));
    }

    @Override
    public <B> ApplyNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitApply(
                meta(),
                transformer.visitExpr(expr()),
                args().collect(transformer::visitExpr));
    }
}

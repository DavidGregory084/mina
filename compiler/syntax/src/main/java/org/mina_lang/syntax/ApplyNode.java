package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record ApplyNode<A> (Meta<A> meta, ExprNode<A> expr, ImmutableList<ExprNode<A>> args) implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        expr.accept(visitor);
        args.forEach(arg -> arg.accept(visitor));
        visitor.visitApply(this);
    }
}

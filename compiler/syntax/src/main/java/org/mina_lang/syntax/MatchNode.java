package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record MatchNode<A> (Meta<A> meta, ExprNode<A> scrutinee, ImmutableList<CaseNode<A>> cases)
        implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        scrutinee.accept(visitor);
        cases.forEach(cse -> cse.accept(visitor));
        visitor.visitMatch(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitMatch(
                meta(),
                visitor.visitExpr(scrutinee()),
                cases().collect(cse -> cse.accept(visitor)));
    }

    @Override
    public <B> MatchNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitMatch(
                meta(),
                transformer.visitExpr(scrutinee()),
                cases().collect(cse -> cse.accept(transformer)));
    }
}

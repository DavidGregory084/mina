package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public record LambdaNode<A> (Meta<A> meta, ImmutableList<ParamNode<A>> params, ExprNode<A> body)
        implements ExprNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        params.forEach(param -> param.accept(visitor));
        body.accept(visitor);
        visitor.visitLambda(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitLambda(this);

        var result = visitor.visitLambda(
                meta(),
                params().collect(param -> param.accept(visitor)),
                visitor.visitExpr(body()));

        visitor.postVisitLambda(this);

        return result;
    }

    @Override
    public <B> LambdaNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitLambda(this);

        var result = visitor.visitLambda(
                meta(),
                params().collect(param -> param.accept(visitor)),
                visitor.visitExpr(body()));

        visitor.postVisitLambda(result);

        return result;
    }
}

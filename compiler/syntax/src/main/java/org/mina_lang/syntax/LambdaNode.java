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
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitLambda(
                meta(),
                params().collect(param -> param.accept(visitor)),
                visitor.visitExpr(body()));
    }

    @Override
    public <B> LambdaNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitLambda(
                meta(),
                params().collect(param -> param.accept(transformer)),
                transformer.visitExpr(body()));
    }
}

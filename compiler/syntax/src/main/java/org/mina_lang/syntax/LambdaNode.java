package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record LambdaNode<A>(Meta<A> meta, ImmutableList<ParamNode<A>> params, ExprNode<A> body) implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        params.forEach(param -> param.accept(visitor));
        body.accept(visitor);
        visitor.visitLambda(this);
    }
}

package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public record TypeLambdaNode<A> (Meta<A> meta, ImmutableList<TypeVarNode<A>> args, TypeNode<A> body)
        implements TypeNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        args.forEach(arg -> arg.accept(visitor));
        body.accept(visitor);
        visitor.visitTypeLambda(this);
    }

    @Override
    public <B> B accept(TypeNodeFolder<A, B> visitor) {
        visitor.preVisitTypeLambda(this);

        var result = visitor.visitTypeLambda(
                meta(),
                args().collect(visitor::visitTypeVar),
                visitor.visitType(body()));

        visitor.postVisitTypeLambda(result);

        return result;
    }

    @Override
    public <B> TypeLambdaNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitTypeLambda(this);

        var result = visitor.visitTypeLambda(
                meta(),
                args().collect(visitor::visitTypeVar),
                visitor.visitType(body()));

        visitor.postVisitTypeLambda(result);

        return result;
    }
}

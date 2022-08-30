package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

public record TypeLambdaNode<A> (Meta<A> meta, ImmutableList<TypeVarNode<A>> args, TypeNode<A> body)
        implements TypeNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        args.forEach(arg -> arg.accept(visitor));
        body.accept(visitor);
        visitor.visitTypeLambda(this);
    }
}

package org.mina_lang.syntax;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;

public record LetFnNode<A> (Meta<A> meta, String name, ImmutableList<TypeVarNode<A>> typeParams,
        ImmutableList<ParamNode<A>> valueParams, Optional<TypeNode<A>> returnType, ExprNode<A> expr)
        implements DeclarationNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        // Visit types
        typeParams.forEach(tyParam -> tyParam.accept(visitor));
        returnType.ifPresent(returnTy -> returnTy.accept(visitor));
        // Visit values
        valueParams.forEach(param -> param.accept(visitor));
        expr.accept(visitor);

        visitor.visitLetFn(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitLetFn(
                meta(),
                name(),
                typeParams().collect(visitor::visitTypeVar),
                valueParams().collect(param -> param.accept(visitor)),
                returnType().map(visitor::visitType),
                visitor.visitExpr(expr()));
    }

    @Override
    public <B> LetFnNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitLetFn(
                meta(),
                name(),
                typeParams().collect(transformer::visitTypeVar),
                valueParams().collect(param -> param.accept(transformer)),
                returnType().map(transformer::visitType),
                transformer.visitExpr(expr()));
    }
}

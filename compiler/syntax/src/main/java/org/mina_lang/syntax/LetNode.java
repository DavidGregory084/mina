package org.mina_lang.syntax;

import java.util.Optional;

public record LetNode<A> (Meta<A> meta, String name, Optional<TypeNode<A>> type, ExprNode<A> expr)
        implements DeclarationNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        type.ifPresent(typ -> typ.accept(visitor));
        expr.accept(visitor);
        visitor.visitLet(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitLet(
                meta(),
                name(),
                type().map(visitor::visitType),
                visitor.visitExpr(expr()));
    }

    @Override
    public <B> LetNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitLet(
                meta(),
                name(),
                type().map(transformer::visitType),
                transformer.visitExpr(expr()));
    }
}

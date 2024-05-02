package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record SelectNode<A>(Meta<A> meta, ExprNode<A> receiver, ReferenceNode<A> selection) implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        receiver.accept(visitor);
        selection.accept(visitor);
        visitor.visitSelect(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitSelect(this);

        var result = visitor.visitSelect(
            meta(),
            visitor.visitExpr(receiver()),
            visitor.visitExpr(selection()));

        visitor.postVisitSelect(this);

        return result;
    }

    @Override
    public <B> SelectNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitSelect(this);

        var result = visitor.visitSelect(
            meta(),
            visitor.visitExpr(receiver()),
            selection().accept(visitor));

        visitor.postVisitSelect(result);

        return result;
    }
}

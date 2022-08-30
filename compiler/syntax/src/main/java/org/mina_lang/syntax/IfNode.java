package org.mina_lang.syntax;

public record IfNode<A>(Meta<A> meta, ExprNode<A> condition, ExprNode<A> consequent, ExprNode<A> alternative) implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        condition.accept(visitor);
        consequent.accept(visitor);
        alternative.accept(visitor);
        visitor.visitIf(this);
    }
}

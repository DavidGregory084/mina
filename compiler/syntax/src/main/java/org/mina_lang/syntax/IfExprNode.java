package org.mina_lang.syntax;

public record IfExprNode<A>(Meta<A> meta, ExprNode<A> condition, ExprNode<A> consequent, ExprNode<A> alternative) implements ExprNode<A> {

}
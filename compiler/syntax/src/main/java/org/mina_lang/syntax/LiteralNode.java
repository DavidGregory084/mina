package org.mina_lang.syntax;

public sealed interface LiteralNode<A> extends ExprNode<A> permits LiteralBooleanNode<A>, LiteralCharNode<A>, LiteralIntNode<A> {

}

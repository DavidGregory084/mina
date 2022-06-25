package org.mina_lang.syntax;

sealed public interface ExprNode<A> extends SyntaxNode<A> permits IfExprNode<A>, LambdaExprNode<A>, MatchNode<A>, ReferenceNode<A>, LiteralNode<A>, ApplyNode<A> {

}

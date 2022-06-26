package org.mina_lang.syntax;

sealed public interface ExprNode<A> extends SyntaxNode<A> permits IfExprNode, LambdaExprNode, MatchNode, ReferenceNode, LiteralNode, ApplyNode {

}

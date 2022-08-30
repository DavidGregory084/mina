package org.mina_lang.syntax;

sealed public interface ExprNode<A> extends MetaNode<A> permits BlockNode, IfNode, LambdaNode, MatchNode, ReferenceNode, LiteralNode, ApplyNode {

}

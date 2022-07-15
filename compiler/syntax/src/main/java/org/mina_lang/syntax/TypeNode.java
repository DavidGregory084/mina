package org.mina_lang.syntax;

sealed public interface TypeNode<A> extends SyntaxNode<A> permits TypeLambdaNode, TypeApplyNode, TypeReferenceNode {
}

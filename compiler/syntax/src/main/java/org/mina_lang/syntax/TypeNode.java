package org.mina_lang.syntax;

sealed public interface TypeNode<A> extends MetaNode<A> permits TypeLambdaNode, FunTypeNode, TypeApplyNode, TypeVarNode, TypeReferenceNode {
}

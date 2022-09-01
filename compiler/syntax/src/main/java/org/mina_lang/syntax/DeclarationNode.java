package org.mina_lang.syntax;

sealed public interface DeclarationNode<A> extends MetaNode<A>permits DataNode, LetNode, LetFnNode {

    @Override
    <B> DeclarationNode<B> accept(MetaNodeTransformer<A, B> transformer);
}

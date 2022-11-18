package org.mina_lang.syntax;

sealed public interface TypeNode<A>
        extends MetaNode<A>permits TypeLambdaNode, FunTypeNode, TypeApplyNode, TypeVarNode, TypeReferenceNode {

    <B> B accept(TypeNodeFolder<A, B> visitor);

    default <B> B accept(MetaNodeFolder<A, B> visitor) {
        return accept((TypeNodeFolder<A, B>) visitor);
    }

    @Override
    <B> TypeNode<B> accept(MetaNodeTransformer<A, B> transformer);
}

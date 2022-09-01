package org.mina_lang.syntax;

public sealed interface TypeVarNode<A> extends TypeNode<A>permits ExistsVarNode, ForAllVarNode {
    public String name();

    @Override
    <B> TypeVarNode<B> accept(MetaNodeTransformer<A, B> transformer);
}

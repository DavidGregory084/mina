package org.mina_lang.syntax;

public sealed interface TypeVarNode<A> extends TypeNode<A> permits ExistsVarNode, ForAllVarNode {
    public String name();
}

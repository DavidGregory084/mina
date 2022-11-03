package org.mina_lang.syntax;

import org.mina_lang.common.names.TypeVarName;

public sealed interface TypeVarNode<A> extends TypeNode<A>permits ExistsVarNode, ForAllVarNode {
    public String name();

    @Override
    <B> TypeVarNode<B> accept(MetaNodeTransformer<A, B> transformer);

    public default TypeVarName getName() {
        return new TypeVarName(name());
    }
}

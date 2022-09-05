package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record ExistsVarNode<A> (Meta<A> meta, String name) implements TypeVarNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitExistsVar(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitExistsVar(meta(), name());
    }

    @Override
    public <B> ExistsVarNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitExistsVar(meta(), name());
    }
}

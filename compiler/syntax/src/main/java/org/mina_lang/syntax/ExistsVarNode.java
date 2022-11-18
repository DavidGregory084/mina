package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record ExistsVarNode<A> (Meta<A> meta, String name) implements TypeVarNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitExistsVar(this);
    }

    @Override
    public <B> B accept(TypeNodeFolder<A, B> visitor) {
        visitor.preVisitExistsVar(this);
        var result = visitor.visitExistsVar(meta(), name());
        visitor.postVisitExistsVar(result);
        return result;
    }

    @Override
    public <B> ExistsVarNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitExistsVar(this);
        var result = visitor.visitExistsVar(meta(), name());
        visitor.postVisitExistsVar(result);
        return result;
    }
}

package org.mina_lang.syntax;

public record ForAllVarNode<A> (Meta<A> meta, String name) implements TypeVarNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitForAllVar(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitExistsVar(meta(), name());
    }

    @Override
    public <B> ForAllVarNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitForAllVar(meta(), name());
    }
}

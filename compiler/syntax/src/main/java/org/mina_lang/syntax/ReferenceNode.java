package org.mina_lang.syntax;

public record ReferenceNode<A> (Meta<A> meta, QualifiedIdNode<A> id) implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        id.accept(visitor);
        visitor.visitReference(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitReference(meta(), id().accept(visitor));
    }

    @Override
    public <B> ReferenceNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitReference(meta(), id().accept(transformer));
    }
}

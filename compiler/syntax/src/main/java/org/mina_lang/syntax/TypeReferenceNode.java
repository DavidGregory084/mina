package org.mina_lang.syntax;

public record TypeReferenceNode<A> (Meta<A> meta, QualifiedIdNode<A> id) implements TypeNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        id.accept(visitor);
        visitor.visitTypeReference(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitTypeReference(meta(), id().accept(visitor));
    }

    @Override
    public <B> TypeReferenceNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitTypeReference(meta(), id().accept(transformer));
    }
}

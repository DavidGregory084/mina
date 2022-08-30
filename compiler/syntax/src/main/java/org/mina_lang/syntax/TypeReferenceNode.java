package org.mina_lang.syntax;

public record TypeReferenceNode<A> (Meta<A> meta, QualifiedIdNode<A> id) implements TypeNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        id.accept(visitor);
        visitor.visitTypeReference(this);
    }
}

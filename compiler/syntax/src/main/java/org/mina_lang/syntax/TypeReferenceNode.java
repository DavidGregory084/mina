package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record TypeReferenceNode<A> (Meta<A> meta, QualifiedIdNode id) implements TypeNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        id.accept(visitor);
        visitor.visitTypeReference(this);
    }

    @Override
    public <B> B accept(TypeNodeFolder<A, B> visitor) {
        visitor.preVisitTypeReference(this);
        var result = visitor.visitTypeReference(meta(), id());
        visitor.preVisitTypeReference(this);
        return result;
    }

    @Override
    public <B> TypeReferenceNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitTypeReference(this);
        var result = visitor.visitTypeReference(meta(), id());
        visitor.preVisitTypeReference(this);
        return result;
    }
}

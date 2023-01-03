package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record ReferenceNode<A> (Meta<A> meta, QualifiedIdNode id) implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        id.accept(visitor);
        visitor.visitReference(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitReference(this);
        var result = visitor.visitReference(meta(), id());
        visitor.postVisitReference(this);
        return result;
    }

    @Override
    public <B> ReferenceNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitReference(this);
        var result = visitor.visitReference(meta(), id());
        visitor.postVisitReference(result);
        return result;
    }
}

package org.mina_lang.syntax;

import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.FieldName;
import org.mina_lang.common.Meta;

public record ConstructorParamNode<A> (Meta<A> meta, String name, TypeNode<A> typeAnnotation)
        implements MetaNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        typeAnnotation.accept(visitor);
        visitor.visitConstructorParam(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        return accept((DataNodeFolder<A, B>) visitor);
    }

    public <B> B accept(DataNodeFolder<A, B> visitor) {
        visitor.preVisitConstructorParam(this);

        var result = visitor.visitConstructorParam(
            meta(),
            name(),
            visitor.visitType(typeAnnotation()));

        visitor.postVisitConstructorParam(result);

        return result;
    }

    @Override
    public <B> ConstructorParamNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        return accept((DataNodeTransformer<A, B>) visitor);
    }

    public <B> ConstructorParamNode<B> accept(DataNodeTransformer<A, B> visitor) {
        visitor.preVisitConstructorParam(this);

        var result = visitor.visitConstructorParam(
            meta(),
            name(),
            visitor.visitType(typeAnnotation()));

        visitor.postVisitConstructorParam(result);

        return result;
    }

    public FieldName getName(ConstructorName constr) {
        return new FieldName(constr, name());
    }
}

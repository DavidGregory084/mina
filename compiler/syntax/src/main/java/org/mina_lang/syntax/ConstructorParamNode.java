package org.mina_lang.syntax;

public record ConstructorParamNode<A> (Meta<A> meta, String name, TypeNode<Void> typeAnnotation)
        implements MetaNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        typeAnnotation.accept(visitor);
        visitor.visitConstructorParam(this);
    }
}

package org.mina_lang.syntax;

import java.util.Optional;

public record ParamNode<A>(Meta<A> meta, String name, Optional<TypeNode<Void>> typeAnnotation) implements MetaNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        typeAnnotation.ifPresent(tyAnn -> tyAnn.accept(visitor));
        visitor.visitParam(this);
    }
}

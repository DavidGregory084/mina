package org.mina_lang.syntax;

public record ConstructorParamNode<A> (Meta<A> meta, String name, TypeNode<Void> typeAnnotation)
        implements SyntaxNode<A> {

}

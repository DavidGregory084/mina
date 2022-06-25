package org.mina_lang.syntax;

public record DataDeclarationNode<A>(Meta<A> meta, String name) implements DeclarationNode<A> {

}

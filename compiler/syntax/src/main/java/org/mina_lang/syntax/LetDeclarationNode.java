package org.mina_lang.syntax;

public record LetDeclarationNode<A>(Meta<A> meta, String name, ExprNode<A> expr) implements DeclarationNode<A> {

}

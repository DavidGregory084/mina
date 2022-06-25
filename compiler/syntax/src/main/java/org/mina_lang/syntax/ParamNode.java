package org.mina_lang.syntax;

public record ParamNode<A>(Meta<A> meta, String name) implements SyntaxNode<A> {

}

package org.mina_lang.syntax;

public record LiteralStringNode<A>(Meta<A> meta, String value) implements LiteralNode<A> {

}

package org.mina_lang.syntax;

public record FieldPatternNode<A>(Meta<A> meta, String field, PatternNode<A> pattern) implements SyntaxNode<A> {

}

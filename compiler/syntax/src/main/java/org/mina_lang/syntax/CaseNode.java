package org.mina_lang.syntax;

public record CaseNode<A>(Meta<A> meta, PatternNode<A> pattern, ExprNode<A> consequent) implements SyntaxNode<A> {

}

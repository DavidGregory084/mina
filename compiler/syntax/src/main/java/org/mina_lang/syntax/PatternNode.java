package org.mina_lang.syntax;

public sealed interface PatternNode<A> extends SyntaxNode<A> permits ConstructorPatternNode, IdPatternNode {

}

package org.mina_lang.syntax;

public sealed interface PatternNode<A> extends MetaNode<A> permits ConstructorPatternNode, IdPatternNode, LiteralPatternNode {

}

package org.mina_lang.syntax;

import java.util.Optional;

public record LiteralPatternNode<A>(Meta<A> meta, Optional<String> alias, LiteralNode<A> literal) implements PatternNode<A> {

}

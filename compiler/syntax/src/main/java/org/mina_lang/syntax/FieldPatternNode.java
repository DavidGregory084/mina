package org.mina_lang.syntax;

import java.util.Optional;

public record FieldPatternNode<A>(Meta<A> meta, String field, Optional<PatternNode<A>> pattern) implements SyntaxNode<A> {

}

package org.mina_lang.syntax;

import java.util.Optional;

public record FieldPatternNode<A>(Meta<A> meta, String field, Optional<PatternNode<A>> pattern) implements MetaNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        pattern.ifPresent(pat -> pat.accept(visitor));
        visitor.visitFieldPattern(this);
    }
}

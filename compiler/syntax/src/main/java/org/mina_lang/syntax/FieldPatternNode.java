package org.mina_lang.syntax;

import java.util.Optional;

public record FieldPatternNode<A> (Meta<A> meta, String field, Optional<PatternNode<A>> pattern)
        implements MetaNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        pattern.ifPresent(pat -> pat.accept(visitor));
        visitor.visitFieldPattern(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitFieldPattern(
                meta(),
                field(),
                pattern().map(visitor::visitPattern));
    }

    @Override
    public <B> FieldPatternNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitFieldPattern(
                meta(),
                field(),
                pattern().map(transformer::visitPattern));
    }
}

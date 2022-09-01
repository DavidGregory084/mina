package org.mina_lang.syntax;

import java.util.Optional;

public record IdPatternNode<A> (Meta<A> meta, Optional<String> alias, String name) implements PatternNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitIdPattern(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitIdPattern(
                meta(),
                alias(),
                name());
    }

    @Override
    public <B> IdPatternNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitIdPattern(
                meta(),
                alias(),
                name());
    }
}

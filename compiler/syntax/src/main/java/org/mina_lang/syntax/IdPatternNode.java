package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record IdPatternNode<A> (Meta<A> meta, String name) implements PatternNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitIdPattern(this);
    }

    @Override
    public <B> B accept(PatternNodeFolder<A, B> visitor) {
        visitor.preVisitIdPattern(this);

        var result = visitor.visitIdPattern(meta(), name());

        visitor.postVisitIdPattern(this);

        return result;
    }

    @Override
    public <B> IdPatternNode<B> accept(PatternNodeTransformer<A, B> visitor) {
        visitor.preVisitIdPattern(this);

        var result = visitor.visitIdPattern(meta(), name());

        visitor.postVisitIdPattern(result);

        return result;
    }
}

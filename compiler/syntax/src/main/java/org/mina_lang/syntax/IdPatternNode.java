package org.mina_lang.syntax;

import java.util.Optional;

import org.mina_lang.common.Meta;

public record IdPatternNode<A> (Meta<A> meta, String name) implements PatternNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitIdPattern(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitIdPattern(this);

        var result = visitor.visitIdPattern(meta(), name());

        visitor.postVisitIdPattern(result);

        return result;
    }

    @Override
    public <B> IdPatternNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitIdPattern(this);

        var result = visitor.visitIdPattern(meta(), name());

        visitor.postVisitIdPattern(result);

        return result;
    }
}

package org.mina_lang.syntax;

import java.util.Optional;

public record LiteralPatternNode<A>(Meta<A> meta, Optional<String> alias, LiteralNode<A> literal) implements PatternNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        literal.accept(visitor);
        visitor.visitLiteralPattern(this);
    }

    @Override
    public <B> B accept(MetaNodeVisitor<A, B> visitor) {
        return visitor.visitLiteralPattern(
            meta(),
            alias(),
            visitor.visitLiteral(literal()));
    }

    @Override
    public <B> LiteralPatternNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return transformer.visitLiteralPattern(
            meta(),
            alias(),
            transformer.visitLiteral(literal()));
    }
}

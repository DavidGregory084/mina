package org.mina_lang.syntax;

import java.util.Optional;

import org.mina_lang.common.Meta;

public record LiteralPatternNode<A>(Meta<A> meta, LiteralNode<A> literal) implements PatternNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        literal.accept(visitor);
        visitor.visitLiteralPattern(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitLiteralPattern(this);

        var result = visitor.visitLiteralPattern(
            meta(),
            visitor.visitLiteral(literal()));

        visitor.postVisitLiteralPattern(result);

        return result;
    }

    @Override
    public <B> LiteralPatternNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitLiteralPattern(this);

        var result = visitor.visitLiteralPattern(
            meta(),
            visitor.visitLiteral(literal()));

        visitor.postVisitLiteralPattern(result);

        return result;
    }
}

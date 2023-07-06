/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record LiteralPatternNode<A>(Meta<A> meta, LiteralNode<A> literal) implements PatternNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        literal.accept(visitor);
        visitor.visitLiteralPattern(this);
    }

    @Override
    public <B> B accept(PatternNodeFolder<A, B> visitor) {
        visitor.preVisitLiteralPattern(this);

        var result = visitor.visitLiteralPattern(
            meta(),
            visitor.visitLiteral(literal()));

        visitor.postVisitLiteralPattern(this);

        return result;
    }

    @Override
    public <B> LiteralPatternNode<B> accept(PatternNodeTransformer<A, B> visitor) {
        visitor.preVisitLiteralPattern(this);

        var result = visitor.visitLiteralPattern(
            meta(),
            visitor.visitLiteral(literal()));

        visitor.postVisitLiteralPattern(result);

        return result;
    }
}

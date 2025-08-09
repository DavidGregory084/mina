/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

import java.util.List;

public record ConstructorPatternNode<A>(Meta<A> meta, QualifiedIdNode id,
        List<FieldPatternNode<A>> fields) implements PatternNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        id.accept(visitor);
        fields.forEach(field -> field.accept(visitor));
        visitor.visitConstructorPattern(this);
    }

    @Override
    public <B> B accept(PatternNodeFolder<A, B> visitor) {
        visitor.preVisitConstructorPattern(this);

        var result = visitor.visitConstructorPattern(
                meta(),
                id(),
                fields().stream().map(field -> field.accept(visitor)).toList());

        visitor.postVisitConstructorPattern(this);

        return result;
    }

    @Override
    public <B> ConstructorPatternNode<B> accept(PatternNodeTransformer<A, B> visitor) {
        visitor.preVisitConstructorPattern(this);

        var result = visitor.visitConstructorPattern(
                meta(),
                id(),
                fields().stream().map(field -> field.accept(visitor)).toList());

        visitor.postVisitConstructorPattern(result);

        return result;
    }
}

/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;


public record FieldPatternNode<A> (Meta<A> meta, String field, PatternNode<A> pattern)
        implements MetaNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        pattern.accept(visitor);
        visitor.visitFieldPattern(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        return accept((PatternNodeFolder<A, B>) visitor);
    }

    public <B> B accept(PatternNodeFolder<A, B> visitor) {
        visitor.preVisitFieldPattern(this);

        var result = visitor.visitFieldPattern(
                meta(),
                field(),
                pattern().accept(visitor));

        visitor.postVisitFieldPattern(this);

        return result;
    }

    @Override
    public <B> FieldPatternNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        return accept((PatternNodeTransformer<A, B>) visitor);
    }

    public <B> FieldPatternNode<B> accept(PatternNodeTransformer<A, B> visitor) {
        visitor.preVisitFieldPattern(this);

        var result = visitor.visitFieldPattern(
                meta(),
                field(),
                pattern().accept(visitor));

        visitor.postVisitFieldPattern(result);

        return result;
    }
}

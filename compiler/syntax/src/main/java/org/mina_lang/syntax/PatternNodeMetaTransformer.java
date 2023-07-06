/*
 * SPDX-FileCopyrightText:  © 2022 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

import java.util.Optional;

import static org.mina_lang.syntax.SyntaxNodes.*;

public interface PatternNodeMetaTransformer<A, B> extends LiteralNodeMetaTransformer<A, B>, PatternNodeTransformer<A, B> {

    @Override
    default AliasPatternNode<B> visitAliasPattern(Meta<A> meta, String alias, PatternNode<B> pattern) {
        return aliasPatternNode(updateMeta(meta), alias, pattern);
    }

    @Override
    default ConstructorPatternNode<B> visitConstructorPattern(Meta<A> meta, QualifiedIdNode id,
            ImmutableList<FieldPatternNode<B>> fields) {
        return constructorPatternNode(updateMeta(meta), id, fields);
    }

    @Override
    default FieldPatternNode<B> visitFieldPattern(Meta<A> meta, String field, Optional<PatternNode<B>> pattern) {
        return fieldPatternNode(updateMeta(meta), field, pattern);
    }

    @Override
    default IdPatternNode<B> visitIdPattern(Meta<A> meta, String name) {
        return idPatternNode(updateMeta(meta), name);
    }

    @Override
    default LiteralPatternNode<B> visitLiteralPattern(Meta<A> meta, LiteralNode<B> literal) {
        return literalPatternNode(updateMeta(meta), literal);
    }
}

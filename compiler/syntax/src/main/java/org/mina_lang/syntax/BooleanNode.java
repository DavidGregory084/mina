/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record BooleanNode<A> (Meta<A> meta, boolean value) implements LiteralNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitBoolean(this);
    }

    @Override
    public <B> B accept(LiteralNodeFolder<A, B> visitor) {
        visitor.preVisitBoolean(this);
        var result = visitor.visitBoolean(meta(), value());
        visitor.postVisitBoolean(this);
        return result;
    }

    @Override
    public <B> BooleanNode<B> accept(LiteralNodeTransformer<A, B> visitor) {
        visitor.preVisitBoolean(this);
        var result = visitor.visitBoolean(meta(), value());
        visitor.postVisitBoolean(result);
        return result;
    }

    @Override
    public Boolean boxedValue() {
        return value();
    }
}

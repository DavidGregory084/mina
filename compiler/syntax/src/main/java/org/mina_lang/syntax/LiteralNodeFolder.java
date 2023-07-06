/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public interface LiteralNodeFolder<A, B> {
    default B visitLiteral(LiteralNode<A> literal) {
        return literal.accept(this);
    }

    default void preVisitBoolean(BooleanNode<A> bool) {}

    B visitBoolean(Meta<A> meta, boolean value);

    default void postVisitBoolean(BooleanNode<A> bool) {}


    default void preVisitChar(CharNode<A> chr) {}

    B visitChar(Meta<A> meta, char value);

    default void postVisitChar(CharNode<A> chr) {}


    default void preVisitString(StringNode<A> str) {}

    B visitString(Meta<A> meta, String value);

    default void postVisitString(StringNode<A> str) {}


    default void preVisitInt(IntNode<A> intgr) {}

    B visitInt(Meta<A> meta, int value);

    default void postVisitInt(IntNode<A> intgr) {}


    default void preVisitLong(LongNode<A> lng) {}

    B visitLong(Meta<A> meta, long value);

    default void postVisitLong(LongNode<A> lng) {}


    default void preVisitFloat(FloatNode<A> flt) {}

    B visitFloat(Meta<A> meta, float value);

    default void postVisitFloat(FloatNode<A> flt) {}


    default void preVisitDouble(DoubleNode<A> dbl) {}

    B visitDouble(Meta<A> meta, double value);

    default void postVisitDouble(DoubleNode<A> dbl) {}
}

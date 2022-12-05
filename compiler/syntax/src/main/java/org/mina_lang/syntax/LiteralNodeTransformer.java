package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public interface LiteralNodeTransformer<A, B> {

    default LiteralNode<B> visitLiteral(LiteralNode<A> literal) {
        return literal.accept(this);
    }

    default void preVisitBoolean(BooleanNode<A> bool) {}

    BooleanNode<B> visitBoolean(Meta<A> meta, boolean value);

    default void postVisitBoolean(BooleanNode<B> bool) {}


    default void preVisitChar(CharNode<A> chr) {}

    CharNode<B> visitChar(Meta<A> meta, char value);

    default void postVisitChar(CharNode<B> chr) {}


    default void preVisitString(StringNode<A> str) {}

    StringNode<B> visitString(Meta<A> meta, String value);

    default void postVisitString(StringNode<B> str) {}


    default void preVisitInt(IntNode<A> intgr) {}

    IntNode<B> visitInt(Meta<A> meta, int value);

    default void postVisitInt(IntNode<B> intgr) {}


    default void preVisitLong(LongNode<A> lng) {}

    LongNode<B> visitLong(Meta<A> meta, long value);

    default void postVisitLong(LongNode<B> lng) {}


    default void preVisitFloat(FloatNode<A> flt) {}

    FloatNode<B> visitFloat(Meta<A> meta, float value);

    default void postVisitFloat(FloatNode<B> flt) {}


    default void preVisitDouble(DoubleNode<A> dbl) {}

    DoubleNode<B> visitDouble(Meta<A> meta, double value);

    default void postVisitDouble(DoubleNode<B> dbl) {}
}

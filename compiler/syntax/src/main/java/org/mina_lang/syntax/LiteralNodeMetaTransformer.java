package org.mina_lang.syntax;

import org.mina_lang.common.Meta;
import static org.mina_lang.syntax.SyntaxNodes.*;

public interface LiteralNodeMetaTransformer<A, B> extends MetaTransformer<A, B>, LiteralNodeTransformer<A, B> {
    @Override
    default public BooleanNode<B> visitBoolean(Meta<A> meta, boolean value) {
        return boolNode(updateMeta(meta), value);
    }

    @Override
    default public CharNode<B> visitChar(Meta<A> meta, char value) {
        return charNode(updateMeta(meta), value);
    }

    @Override
    default public DoubleNode<B> visitDouble(Meta<A> meta, double value) {
        return doubleNode(updateMeta(meta), value);
    }

    @Override
    default public FloatNode<B> visitFloat(Meta<A> meta, float value) {
        return floatNode(updateMeta(meta), value);
    }

    @Override
    default public IntNode<B> visitInt(Meta<A> meta, int value) {
        return intNode(updateMeta(meta), value);
    }

    @Override
    default public LongNode<B> visitLong(Meta<A> meta, long value) {
        return longNode(updateMeta(meta), value);
    }

    @Override
    default public StringNode<B> visitString(Meta<A> meta, String value) {
        return stringNode(updateMeta(meta), value);
    }
}

package org.mina_lang.syntax;

public sealed interface LiteralNode<A>
        extends ExprNode<A>permits BooleanNode, CharNode, StringNode, IntNode, LongNode, FloatNode, DoubleNode {

    Object boxedValue();

    <B> B accept(LiteralNodeFolder<A, B> folder);

    @Override
    default <B> B accept(MetaNodeFolder<A, B> folder) {
        return accept((LiteralNodeFolder<A, B>) folder);
    }

    <B> LiteralNode<B> accept(LiteralNodeTransformer<A, B> transformer);

    @Override
    default <B> LiteralNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return accept((LiteralNodeTransformer<A, B>) transformer);
    }
}

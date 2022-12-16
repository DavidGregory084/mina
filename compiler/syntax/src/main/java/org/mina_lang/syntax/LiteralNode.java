package org.mina_lang.syntax;

public sealed interface LiteralNode<A>
        extends ExprNode<A>permits BooleanNode, CharNode, StringNode, IntNode, LongNode, FloatNode, DoubleNode {

    <B> LiteralNode<B> accept(LiteralNodeTransformer<A, B> transformer);

    @Override
    default <B> LiteralNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return accept((LiteralNodeTransformer<A, B>) transformer);
    }
}

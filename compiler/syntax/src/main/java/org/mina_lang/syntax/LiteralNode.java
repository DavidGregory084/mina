package org.mina_lang.syntax;

public sealed interface LiteralNode<A>
        extends ExprNode<A>permits BooleanNode, CharNode, StringNode, IntNode, LongNode, FloatNode, DoubleNode {

    @Override
    <B> LiteralNode<B> accept(MetaNodeTransformer<A, B> transformer);
}

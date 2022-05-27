package org.mina_lang.syntax;

public sealed interface LiteralNode extends ExprNode permits LiteralBooleanNode, LiteralCharNode, LiteralIntNode {
    
}

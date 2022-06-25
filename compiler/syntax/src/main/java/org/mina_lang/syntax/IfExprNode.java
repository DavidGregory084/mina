package org.mina_lang.syntax;

public record IfExprNode(ExprNode condition, ExprNode consequent, ExprNode alternative) implements ExprNode {
    
}
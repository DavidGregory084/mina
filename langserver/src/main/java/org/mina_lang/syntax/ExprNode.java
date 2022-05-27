package org.mina_lang.syntax;

sealed public interface ExprNode extends SyntaxNode permits IfExprNode, LambdaExprNode, ReferenceNode, LiteralNode, ApplyNode {
    
}

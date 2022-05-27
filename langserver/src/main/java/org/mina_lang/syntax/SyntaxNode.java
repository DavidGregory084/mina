package org.mina_lang.syntax;

public sealed interface SyntaxNode permits CompilationUnitNode, ModuleNode, ImportNode, DeclarationNode, ExprNode, ParamNode {
}

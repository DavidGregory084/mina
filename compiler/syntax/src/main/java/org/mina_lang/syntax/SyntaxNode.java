package org.mina_lang.syntax;

public sealed interface SyntaxNode<A> permits CompilationUnitNode, ModuleNode, ImportNode, DeclarationNode, ExprNode, ParamNode, CaseNode, PatternNode, FieldPatternNode, QualifiedIdNode, TypeNode {
    public Meta<A> meta();
}

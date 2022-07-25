package org.mina_lang.syntax;

public sealed interface SyntaxNode<A> permits CompilationUnitNode, ModuleNode, ImportNode, DeclarationNode, ConstructorNode, ConstructorParamNode, ExprNode, ParamNode, CaseNode, PatternNode, FieldPatternNode, ModuleIdNode, QualifiedIdNode, TypeNode {
    public Meta<A> meta();
}

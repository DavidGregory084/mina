package org.mina_lang.syntax;

public sealed interface SyntaxNode<A> permits CompilationUnitNode<A>, ModuleNode<A>, ImportNode<A>, DeclarationNode<A>, ExprNode<A>, ParamNode<A>, CaseNode<A>, PatternNode<A>, FieldPatternNode<A>, QualifiedIdNode<A> {
    public Meta<A> meta();
}

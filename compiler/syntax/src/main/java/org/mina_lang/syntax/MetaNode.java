package org.mina_lang.syntax;

import org.mina_lang.common.Range;

public sealed interface MetaNode<A> extends SyntaxNode permits CompilationUnitNode, ModuleNode, ImportNode, DeclarationNode, ConstructorNode, ConstructorParamNode, ExprNode, ParamNode, CaseNode, PatternNode, FieldPatternNode, ModuleIdNode, QualifiedIdNode, TypeNode {
    public Meta<A> meta();

    @Override
    default Range range() {
        return meta().range();
    }
}

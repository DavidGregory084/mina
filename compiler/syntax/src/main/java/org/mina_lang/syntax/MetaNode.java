package org.mina_lang.syntax;

import org.mina_lang.common.Range;

public sealed interface MetaNode<A> extends SyntaxNode permits NamespaceNode, ImportNode, DeclarationNode, ConstructorNode, ConstructorParamNode, ExprNode, ParamNode, CaseNode, PatternNode, FieldPatternNode, NamespaceIdNode, QualifiedIdNode, TypeNode {
    public Meta<A> meta();

    @Override
    default Range range() {
        return meta().range();
    }
}

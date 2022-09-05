package org.mina_lang.syntax;

import org.mina_lang.common.Meta;
import org.mina_lang.common.Range;

public sealed interface MetaNode<A> extends SyntaxNode permits NamespaceNode, DeclarationNode, ConstructorNode, ConstructorParamNode, ExprNode, ParamNode, CaseNode, PatternNode, FieldPatternNode, QualifiedIdNode, TypeNode {
    public Meta<A> meta();

    @Override
    default Range range() {
        return meta().range();
    }

    <B> B accept(MetaNodeVisitor<A, B> visitor);

    <B> MetaNode<B> accept(MetaNodeTransformer<A, B> transformer);
}

package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

sealed public interface ExprNode<A>
        extends MetaNode<A>permits BlockNode, IfNode, LambdaNode, MatchNode, ReferenceNode, LiteralNode, ApplyNode {

    @Override
    <B> ExprNode<B> accept(MetaNodeTransformer<A, B> transformer);
}

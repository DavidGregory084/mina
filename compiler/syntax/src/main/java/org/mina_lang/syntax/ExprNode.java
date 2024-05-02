/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

sealed public interface ExprNode<A>
        extends MetaNode<A>permits BlockNode, IfNode, LambdaNode, MatchNode, ReferenceNode, LiteralNode, ApplyNode, SelectNode {

    @Override
    <B> ExprNode<B> accept(MetaNodeTransformer<A, B> transformer);
}

/*
 * SPDX-FileCopyrightText:  © 2022-2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

sealed public interface ExprNode<A>
        extends MetaNode<A> permits ApplyNode, BinaryOpNode, BlockNode, IfNode, LambdaNode, LiteralNode, MatchNode, ReferenceNode, SelectNode, UnaryOpNode {

    @Override
    <B> ExprNode<B> accept(MetaNodeTransformer<A, B> transformer);
}

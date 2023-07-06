/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

sealed public interface DeclarationNode<A> extends MetaNode<A>permits DataNode, LetNode, LetFnNode {

    String name();

    @Override
    <B> DeclarationNode<B> accept(MetaNodeTransformer<A, B> transformer);
}

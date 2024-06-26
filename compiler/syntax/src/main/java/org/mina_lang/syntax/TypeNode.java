/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

sealed public interface TypeNode<A>
        extends MetaNode<A>permits QuantifiedTypeNode, FunTypeNode, TypeApplyNode, TypeVarNode, TypeReferenceNode {

    <B> B accept(TypeNodeFolder<A, B> visitor);

    default <B> B accept(MetaNodeFolder<A, B> visitor) {
        return accept((TypeNodeFolder<A, B>) visitor);
    }

    <B> TypeNode<B> accept(TypeNodeTransformer<A, B> transformer);

    @Override
    default <B> TypeNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        return accept((TypeNodeTransformer<A, B>) visitor);
    }
}

/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.names.TypeVarName;

public sealed interface TypeVarNode<A> extends TypeNode<A>permits ExistsVarNode, ForAllVarNode {
    public String name();

    @Override
    <B> TypeVarNode<B> accept(TypeNodeTransformer<A, B> transformer);

    @Override
    default <B> TypeVarNode<B> accept(MetaNodeTransformer<A, B> transformer) {
        return accept((TypeNodeTransformer<A, B>) transformer);
    }

    public TypeVarName getName();
}

/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

import java.util.List;
import java.util.Optional;

import static org.mina_lang.syntax.SyntaxNodes.*;

public interface DataNodeMetaTransformer<A, B> extends DataNodeTransformer<A, B>, TypeNodeMetaTransformer<A, B> {

    @Override
    default public DataNode<B> visitData(Meta<A> meta, String name,
            List<TypeVarNode<B>> typeParams,
            List<ConstructorNode<B>> constructors) {
        return dataNode(updateMeta(meta), name, typeParams, constructors);
    }

    @Override
    default public ConstructorNode<B> visitConstructor(Meta<A> meta, String name,
            List<ConstructorParamNode<B>> params, Optional<TypeNode<B>> type) {
        return constructorNode(updateMeta(meta), name, params, type);
    }

    @Override
    default public ConstructorParamNode<B> visitConstructorParam(Meta<A> meta, String name, TypeNode<B> typeAnnotation) {
        return constructorParamNode(updateMeta(meta), name, typeAnnotation);
    }
}

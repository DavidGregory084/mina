/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

import static org.mina_lang.syntax.SyntaxNodes.*;

public interface TypeNodeMetaTransformer<A, B> extends MetaTransformer<A, B>, TypeNodeTransformer<A, B> {

    @Override
    default public TypeLambdaNode<B> visitTypeLambda(Meta<A> meta,
            ImmutableList<TypeVarNode<B>> args, TypeNode<B> body) {
        return typeLambdaNode(updateMeta(meta), args, body);
    }

    @Override
    default public FunTypeNode<B> visitFunType(Meta<A> meta, ImmutableList<TypeNode<B>> argTypes,
            TypeNode<B> returnType) {
        return funTypeNode(updateMeta(meta), argTypes, returnType);
    }

    @Override
    default public TypeApplyNode<B> visitTypeApply(Meta<A> meta, TypeNode<B> type,
            ImmutableList<TypeNode<B>> args) {
        return typeApplyNode(updateMeta(meta), type, args);
    }

    @Override
    default public TypeReferenceNode<B> visitTypeReference(Meta<A> meta, QualifiedIdNode id) {
        return typeRefNode(updateMeta(meta), id);
    }

    @Override
    default public ForAllVarNode<B> visitForAllVar(Meta<A> meta, String name) {
        return forAllVarNode(updateMeta(meta), name);
    }

    @Override
    default public ExistsVarNode<B> visitExistsVar(Meta<A> meta, String name) {
        return existsVarNode(updateMeta(meta), name);
    }
}

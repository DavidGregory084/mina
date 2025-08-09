/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

import java.util.List;

import static org.mina_lang.syntax.SyntaxNodes.*;

public interface TypeNodeMetaTransformer<A, B> extends MetaTransformer<A, B>, TypeNodeTransformer<A, B> {

    @Override
    default public QuantifiedTypeNode<B> visitQuantifiedType(Meta<A> meta,
                                                             List<TypeVarNode<B>> args, TypeNode<B> body) {
        return quantifiedTypeNode(updateMeta(meta), args, body);
    }

    @Override
    default public FunTypeNode<B> visitFunType(Meta<A> meta, List<TypeNode<B>> argTypes,
            TypeNode<B> returnType) {
        return funTypeNode(updateMeta(meta), argTypes, returnType);
    }

    @Override
    default public TypeApplyNode<B> visitTypeApply(Meta<A> meta, TypeNode<B> type,
            List<TypeNode<B>> args) {
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

/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

import java.util.List;

public interface TypeNodeTransformer<A, B> {

    default TypeNode<B> visitType(TypeNode<A> typ) {
        return typ.accept(this);
    }

    default void preVisitQuantifiedType(QuantifiedTypeNode<A> quant) {}

    QuantifiedTypeNode<B> visitQuantifiedType(Meta<A> meta, List<TypeVarNode<B>> args, TypeNode<B> body);

    default void postVisitQuantifiedType(QuantifiedTypeNode<B> quant) {}


    default void preVisitFunType(FunTypeNode<A> funTyp) {}

    FunTypeNode<B> visitFunType(Meta<A> meta, List<TypeNode<B>> argTypes, TypeNode<B> returnType);

    default void postVisitFunType(FunTypeNode<B> funTyp) {}


    default void preVisitTypeApply(TypeApplyNode<A> tyApp) {}

    TypeApplyNode<B> visitTypeApply(Meta<A> meta, TypeNode<B> type, List<TypeNode<B>> args);

    default void postVisitTypeApply(TypeApplyNode<B> tyApp) {}


    default void preVisitTypeReference(TypeReferenceNode<A> tyRef) {}

    TypeReferenceNode<B> visitTypeReference(Meta<A> meta, QualifiedIdNode id);

    default void postVisitTypeReference(TypeReferenceNode<B> tyRef) {}


    default TypeVarNode<B> visitTypeVar(TypeVarNode<A> tyVar) {
        return tyVar.accept(this);
    }


    default void preVisitForAllVar(ForAllVarNode<A> forAllVar) {}

    ForAllVarNode<B> visitForAllVar(Meta<A> meta, String name);

    default void postVisitForAllVar(ForAllVarNode<B> forAllVar) {}


    default void preVisitExistsVar(ExistsVarNode<A> existsVar) {}

    ExistsVarNode<B> visitExistsVar(Meta<A> meta, String name);

    default void postVisitExistsVar(ExistsVarNode<B> existsVar) {}
}

/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public interface TypeNodeFolder<A, B> {

    default B visitType(TypeNode<A> typ) {
        return typ.accept(this);
    }

    default void preVisitTypeLambda(TypeLambdaNode<A> tyLam) {}

    B visitTypeLambda(Meta<A> meta, ImmutableList<B> args, B body);

    default void postVisitTypeLambda(B tyLam) {}


    default void preVisitFunType(FunTypeNode<A> funTyp) {}

    B visitFunType(Meta<A> meta, ImmutableList<B> argTypes, B returnType);

    default void postVisitFunType(B funTyp) {}


    default void preVisitTypeApply(TypeApplyNode<A> tyApp) {}

    B visitTypeApply(Meta<A> meta, B type, ImmutableList<B> args);

    default void postVisitTypeApply(B tyApp) {}


    default void preVisitTypeReference(TypeReferenceNode<A> tyRef) {}

    B visitTypeReference(Meta<A> meta, QualifiedIdNode id);

    default void postVisitTypeReference(B tyRef) {}


    default B visitTypeVar(TypeVarNode<A> tyVar) {
        return tyVar.accept(this);
    }

    default void preVisitForAllVar(ForAllVarNode<A> forAllVar) {}

    B visitForAllVar(Meta<A> meta, String name);

    default void postVisitForAllVar(B forAllVar) {}


    default void preVisitExistsVar(ExistsVarNode<A> existsVar) {}

    B visitExistsVar(Meta<A> meta, String name);

    default void postVisitExistsVar(B existsVar) {}
}

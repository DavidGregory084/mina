/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public interface TypeVisitor {
    default void visitType(Type type) {
        type.accept(this);
    }

    default void visitPolyType(PolyType poly) {
        poly.accept(this);
    }

    void visitTypeLambda(TypeLambda tyLam);

    void visitPropositionType(PropositionType propType);

    void visitImplicationType(ImplicationType implType);

    default void visitMonoType(MonoType mono) {
        mono.accept(this);
    }

    void visitTypeConstructor(TypeConstructor tyCon);

    void visitBuiltInType(BuiltInType primTy);

    void visitTypeApply(TypeApply tyApp);

    default void visitTypeVar(TypeVar tyVar) {
        tyVar.accept(this);
    }

    void visitForAllVar(ForAllVar forall);

    void visitExistsVar(ExistsVar exists);

    void visitUnsolvedType(UnsolvedType unsolved);
}

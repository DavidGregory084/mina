/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
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

    void visitQuantifiedType(QuantifiedType quant);

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

    void visitSyntheticVar(SyntheticVar syn);

    void visitUnsolvedType(UnsolvedType unsolved);
}

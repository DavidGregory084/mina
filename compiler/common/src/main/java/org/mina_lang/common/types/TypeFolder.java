/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public interface TypeFolder<A> {
    default A visitType(Type type) {
        return type.accept(this);
    }

    default A visitPolyType(PolyType poly) {
        return poly.accept(this);
    }

    A visitQuantifiedType(QuantifiedType quant);

    A visitPropositionType(PropositionType propType);

    A visitImplicationType(ImplicationType implType);

    default A visitMonoType(MonoType mono) {
        return mono.accept(this);
    }

    A visitTypeConstructor(TypeConstructor tyCon);

    A visitBuiltInType(BuiltInType primTy);

    A visitTypeApply(TypeApply tyApp);

    default A visitTypeVar(TypeVar tyVar) {
        return tyVar.accept(this);
    }

    A visitForAllVar(ForAllVar forall);

    A visitExistsVar(ExistsVar exists);

    A visitUnsolvedType(UnsolvedType unsolved);
}

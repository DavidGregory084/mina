/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public interface TypeTransformer {
    default Type visitType(Type type) {
        return type.accept(this);
    }

    default PolyType visitPolyType(PolyType poly) {
        return poly.accept(this);
    }

    TypeLambda visitTypeLambda(TypeLambda tyLam);

    PropositionType visitPropositionType(PropositionType propType);

    ImplicationType visitImplicationType(ImplicationType implType);

    default MonoType visitMonoType(MonoType mono) {
        return mono.accept(this);
    }

    TypeConstructor visitTypeConstructor(TypeConstructor tyCon);

    BuiltInType visitBuiltInType(BuiltInType primTy);

    TypeApply visitTypeApply(TypeApply tyApp);

    default MonoType visitTypeVar(TypeVar tyVar) {
        return tyVar.accept(this);
    }

    MonoType visitForAllVar(ForAllVar forall);

    MonoType visitExistsVar(ExistsVar exists);

    MonoType visitUnsolvedType(UnsolvedType unsolved);
}

/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

import java.util.Map;
import java.util.Optional;

public class TypeInstantiationTransformer implements TypeTransformer {

    public Map<TypeVar, MonoType> instantiatedVariables;

    public TypeInstantiationTransformer(Map<TypeVar, MonoType> instantiatedVariables) {
        this.instantiatedVariables = instantiatedVariables;
    }

    @Override
    public QuantifiedType visitQuantifiedType(QuantifiedType quant) {
        return new QuantifiedType(
                // Safe to cast here, since we never instantiate
                // variables bound in nested quantifiers
                quant.args().stream().map(tyArg -> (TypeVar) tyArg.accept(this)).toList(),
                quant.body().accept(this),
                quant.kind());
    }

    @Override
    public MonoType visitForAllVar(ForAllVar forall) {
        return Optional
                .ofNullable(instantiatedVariables.get(forall))
                .orElse(forall);
    }

    @Override
    public MonoType visitExistsVar(ExistsVar exists) {
        return Optional
                .ofNullable(instantiatedVariables.get(exists))
                .orElse(exists);
    }

    @Override
    public MonoType visitSyntheticVar(SyntheticVar synVar) {
        return synVar;
    }

    @Override
    public TypeConstructor visitTypeConstructor(TypeConstructor tyCon) {
        return tyCon;
    }

    @Override
    public BuiltInType visitBuiltInType(BuiltInType primTy) {
        return primTy;
    }

    @Override
    public TypeApply visitTypeApply(TypeApply tyApp) {
        return new TypeApply(
                tyApp.type().accept(this),
                tyApp.typeArguments().stream().map(tyArg -> tyArg.accept(this)).toList(),
                tyApp.kind());
    }

    @Override
    public MonoType visitUnsolvedType(UnsolvedType unsolved) {
        return unsolved;
    }
}

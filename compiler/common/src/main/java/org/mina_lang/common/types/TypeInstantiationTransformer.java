/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

import org.eclipse.collections.api.map.ImmutableMap;

import java.util.Optional;

public class TypeInstantiationTransformer implements TypeTransformer {

    public ImmutableMap<TypeVar, MonoType> instantiatedVariables;

    public TypeInstantiationTransformer(ImmutableMap<TypeVar, MonoType> instantiatedVariables) {
        this.instantiatedVariables = instantiatedVariables;
    }

    @Override
    public QuantifiedType visitQuantifiedType(QuantifiedType quant) {
        return new QuantifiedType(
                // Safe to cast here, since we never instantiate
                // variables bound in nested quantifiers
                quant.args().collect(tyArg -> (TypeVar) tyArg.accept(this)),
                quant.body().accept(this),
                quant.kind());
    }

    @Override
    public MonoType visitForAllVar(ForAllVar forall) {
        return Optional
                .<MonoType>ofNullable(instantiatedVariables.get(forall))
                .orElse(forall);
    }

    @Override
    public MonoType visitExistsVar(ExistsVar exists) {
        return Optional
                .<MonoType>ofNullable(instantiatedVariables.get(exists))
                .orElse(exists);
    }

    @Override
    public PropositionType visitPropositionType(PropositionType propType) {
        return new PropositionType(
                propType.type().accept(this),
                propType.property());
    }

    @Override
    public ImplicationType visitImplicationType(ImplicationType implType) {
        return new ImplicationType(
                implType.property(),
                implType.impliedType().accept(this));
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
                tyApp.typeArguments().collect(tyArg -> tyArg.accept(this)),
                tyApp.kind());
    }

    @Override
    public MonoType visitUnsolvedType(UnsolvedType unsolved) {
        return unsolved;
    }
}

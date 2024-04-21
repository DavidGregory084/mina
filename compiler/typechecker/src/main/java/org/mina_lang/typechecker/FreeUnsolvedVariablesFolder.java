/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.typechecker;

import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.eclipse.collections.impl.factory.SortedSets;
import org.mina_lang.common.types.*;

import java.util.Comparator;

public class FreeUnsolvedVariablesFolder implements TypeFolder<ImmutableSortedSet<UnsolvedType>> {

    private static Comparator<UnsolvedType> COMPARATOR = Comparator.comparing(UnsolvedType::id);

    private TypeSubstitutionTransformer typeTransformer;

    public FreeUnsolvedVariablesFolder(TypeSubstitutionTransformer typeTransformer) {
        this.typeTransformer = typeTransformer;
    }

    @Override
    public ImmutableSortedSet<UnsolvedType> visitQuantifiedType(QuantifiedType quant) {
        return quant.body().accept(this);
    }

    @Override
    public ImmutableSortedSet<UnsolvedType> visitTypeConstructor(TypeConstructor tyCon) {
        return SortedSets.immutable.empty(COMPARATOR);
    }

    @Override
    public ImmutableSortedSet<UnsolvedType> visitBuiltInType(BuiltInType primTy) {
        return SortedSets.immutable.empty(COMPARATOR);
    }

    @Override
    public ImmutableSortedSet<UnsolvedType> visitTypeApply(TypeApply tyApp) {
        return tyApp.type().accept(this)
                .newWithAll(tyApp.typeArguments().flatCollect(tyArg -> tyArg.accept(this)));
    }

    @Override
    public ImmutableSortedSet<UnsolvedType> visitForAllVar(ForAllVar forall) {
        return SortedSets.immutable.empty(COMPARATOR);
    }

    @Override
    public ImmutableSortedSet<UnsolvedType> visitExistsVar(ExistsVar exists) {
        return SortedSets.immutable.empty(COMPARATOR);
    }

    @Override
    public ImmutableSortedSet<UnsolvedType> visitUnsolvedType(UnsolvedType unsolved) {
        var substituted = unsolved.accept(typeTransformer);

        if (substituted.equals(unsolved)) {
            return SortedSets.immutable.of(COMPARATOR, unsolved);
        } else {
            return substituted.accept(this);
        }
    }
}

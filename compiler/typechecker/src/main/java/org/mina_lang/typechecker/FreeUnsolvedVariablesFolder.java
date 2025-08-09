/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.typechecker;

import org.mina_lang.common.types.*;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class FreeUnsolvedVariablesFolder implements TypeFolder<SortedSet<UnsolvedType>> {

    private final TypeSubstitutionTransformer typeTransformer;

    public FreeUnsolvedVariablesFolder(TypeSubstitutionTransformer typeTransformer) {
        this.typeTransformer = typeTransformer;
    }

    @Override
    public SortedSet<UnsolvedType> visitQuantifiedType(QuantifiedType quant) {
        return quant.body().accept(this);
    }

    @Override
    public SortedSet<UnsolvedType> visitTypeConstructor(TypeConstructor tyCon) {
        return new TreeSet<>();
    }

    @Override
    public SortedSet<UnsolvedType> visitBuiltInType(BuiltInType primTy) {
        return new TreeSet<>();
    }

    @Override
    public SortedSet<UnsolvedType> visitTypeApply(TypeApply tyApp) {
        var unsolvedInType = tyApp.type().accept(this);
        var unsolvedInArgs = tyApp.typeArguments().stream()
            .flatMap(tyArg -> tyArg.accept(this).stream())
            .collect(Collectors.toCollection(TreeSet::new));
        unsolvedInType.addAll(unsolvedInArgs);
        return unsolvedInType;
    }

    @Override
    public SortedSet<UnsolvedType> visitForAllVar(ForAllVar forall) {
        return new TreeSet<>();
    }

    @Override
    public SortedSet<UnsolvedType> visitExistsVar(ExistsVar exists) {
        return new TreeSet<>();
    }

    @Override
    public SortedSet<UnsolvedType> visitSyntheticVar(SyntheticVar syn) {
        return new TreeSet<>();
    }

    @Override
    public SortedSet<UnsolvedType> visitUnsolvedType(UnsolvedType unsolved) {
        var substituted = unsolved.accept(typeTransformer);

        if (substituted.equals(unsolved)) {
            var unsolvedAfterSubst = new TreeSet<UnsolvedType>();
            unsolvedAfterSubst.add(unsolved);
            return unsolvedAfterSubst;
        } else {
            return substituted.accept(this);
        }
    }
}

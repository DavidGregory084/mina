/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public class TypeSubstitutionTransformer implements TypeTransformer {

    private UnionFind<MonoType> typeSubstitution;
    private KindSubstitutionTransformer kindTransformer;

    public TypeSubstitutionTransformer(KindSubstitutionTransformer kindTransformer) {
        this.kindTransformer = kindTransformer;
    }

    public TypeSubstitutionTransformer(
            UnionFind<MonoType> typeSubstitution,
            KindSubstitutionTransformer kindTransformer) {
        this.typeSubstitution = typeSubstitution;
        this.kindTransformer = kindTransformer;
    }

    public TypeSubstitutionTransformer(
            UnionFind<MonoType> typeSubstitution,
            UnionFind<Kind> kindSubstitution) {
        this.typeSubstitution = typeSubstitution;
        this.kindTransformer = new KindSubstitutionTransformer(kindSubstitution);
    }

    public TypeSubstitutionTransformer(UnionFind<MonoType> typeSubstitution) {
        this.typeSubstitution = typeSubstitution;
    }

    @Override
    public QuantifiedType visitQuantifiedType(QuantifiedType quant) {
        return new QuantifiedType(
                // Safe to cast, since do not manipulate type variables here
                quant.args().collect(tyArg -> (TypeVar) tyArg.accept(this)),
                quant.body().accept(this),
                quant.kind().accept(kindTransformer));
    }

    @Override
    public ForAllVar visitForAllVar(ForAllVar forall) {
        return new ForAllVar(
                forall.name(),
                forall.kind().accept(kindTransformer));
    }

    @Override
    public ExistsVar visitExistsVar(ExistsVar exists) {
        return new ExistsVar(
                exists.name(),
                exists.kind().accept(kindTransformer));
    }

    @Override
    public MonoType visitSyntheticVar(SyntheticVar syn) {
        return new SyntheticVar(
            syn.id(),
            syn.kind().accept(kindTransformer));
    }

    @Override
    public TypeConstructor visitTypeConstructor(TypeConstructor tyCon) {
        return new TypeConstructor(
                tyCon.name(),
                tyCon.kind().accept(kindTransformer));
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
                tyApp.kind().accept(kindTransformer));
    }

    @Override
    public MonoType visitUnsolvedType(UnsolvedType unsolved) {
        var solution = typeSubstitution.find(unsolved);
        if (!solution.equals(unsolved)) {
            return solution.accept(this);
        } else {
            return new UnsolvedType(
                    unsolved.id(),
                    unsolved.kind().accept(kindTransformer));
        }
    }
}

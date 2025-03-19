/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public class SortSubstitutionTransformer implements SortFolder<Sort> {

    private TypeSubstitutionTransformer typeTransformer;

    private KindSubstitutionTransformer kindTransformer;

    public SortSubstitutionTransformer(UnionFind<MonoType> typeSubstitution, KindSubstitutionTransformer kindTransformer) {
        this.kindTransformer = kindTransformer;
        this.typeTransformer = new TypeSubstitutionTransformer(typeSubstitution, kindTransformer);
    }

    public SortSubstitutionTransformer(UnionFind<MonoType> typeSubstitution, UnionFind<Kind> kindSubstitution) {
        this.kindTransformer = new KindSubstitutionTransformer(kindSubstitution);
        this.typeTransformer = new TypeSubstitutionTransformer(typeSubstitution, kindTransformer);
    }

    public TypeSubstitutionTransformer getTypeTransformer() {
        return typeTransformer;
    }

    public KindSubstitutionTransformer getKindTransformer() {
        return kindTransformer;
    }

    @Override
    public Sort visitTypeKind(TypeKind typ) {
        return typ.accept(kindTransformer);
    }

    @Override
    public Sort visitUnsolvedKind(UnsolvedKind unsolved) {
        return unsolved.accept(kindTransformer);
    }

    @Override
    public Sort visitHigherKind(HigherKind higher) {
        return higher.accept(kindTransformer);
    }

    @Override
    public Sort visitQuantifiedType(QuantifiedType quant) {
        return quant.accept(typeTransformer);
    }

    @Override
    public Sort visitTypeConstructor(TypeConstructor tyCon) {
        return tyCon.accept(typeTransformer);
    }

    @Override
    public Sort visitBuiltInType(BuiltInType primTy) {
        return primTy.accept(typeTransformer);
    }

    @Override
    public Sort visitTypeApply(TypeApply tyApp) {
        return tyApp.accept(typeTransformer);
    }

    @Override
    public Sort visitForAllVar(ForAllVar forall) {
        return forall.accept(typeTransformer);
    }

    @Override
    public Sort visitExistsVar(ExistsVar exists) {
        return exists.accept(typeTransformer);
    }

    @Override
    public Sort visitSyntheticVar(SyntheticVar syn) {
        return syn.accept(typeTransformer);
    }

    @Override
    public Sort visitUnsolvedType(UnsolvedType unsolved) {
        return unsolved.accept(typeTransformer);
    }
}

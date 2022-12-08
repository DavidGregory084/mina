package org.mina_lang.common.types;

public class SortSubstitutionTransformer implements SortFolder<Sort> {

    public TypeSubstitutionTransformer typeSubst;
    public KindSubstitutionTransformer kindSubst;

    public SortSubstitutionTransformer(UnionFind<MonoType> typeSubstitution, UnionFind<Kind> kindSubstitution) {
        this.kindSubst = new KindSubstitutionTransformer(kindSubstitution);
        this.typeSubst = new TypeSubstitutionTransformer(typeSubstitution, kindSubst);
    }

    @Override
    public Sort visitTypeKind(TypeKind typ) {
        return typ.accept(kindSubst);
    }

    @Override
    public Sort visitUnsolvedKind(UnsolvedKind unsolved) {
        return unsolved.accept(kindSubst);
    }

    @Override
    public Sort visitHigherKind(HigherKind higher) {
        return higher.accept(kindSubst);
    }

    @Override
    public Sort visitTypeLambda(TypeLambda tyLam) {
        return tyLam.accept(typeSubst);
    }

    @Override
    public Sort visitPropositionType(PropositionType propType) {
        return propType.accept(typeSubst);
    }

    @Override
    public Sort visitImplicationType(ImplicationType implType) {
        return implType.accept(typeSubst);
    }

    @Override
    public Sort visitTypeConstructor(TypeConstructor tyCon) {
        return tyCon.accept(typeSubst);
    }

    @Override
    public Sort visitBuiltInType(BuiltInType primTy) {
        return primTy.accept(typeSubst);
    }

    @Override
    public Sort visitTypeApply(TypeApply tyApp) {
        return tyApp.accept(typeSubst);
    }

    @Override
    public Sort visitForAllVar(ForAllVar forall) {
        return forall.accept(typeSubst);
    }

    @Override
    public Sort visitExistsVar(ExistsVar exists) {
        return exists.accept(typeSubst);
    }

    @Override
    public Sort visitUnsolvedType(UnsolvedType unsolved) {
        return unsolved.accept(typeSubst);
    }
}

package org.mina_lang.common.types;

import org.eclipse.collections.api.factory.Maps;

import java.util.Map;

public class TypeSubstitutionTransformer implements TypeTransformer {

    private Map<UnsolvedType, MonoType> typeSubstitution = Maps.mutable.empty();
    private KindSubstitutionTransformer kindTransformer;

    public TypeSubstitutionTransformer(KindSubstitutionTransformer kindTransformer) {
        this.kindTransformer = kindTransformer;
    }

    public TypeSubstitutionTransformer(
            Map<UnsolvedType, MonoType> typeSubstitution,
            KindSubstitutionTransformer kindTransformer) {
        this.typeSubstitution = typeSubstitution;
        this.kindTransformer = kindTransformer;
    }

    public TypeSubstitutionTransformer(
            Map<UnsolvedType, MonoType> typeSubstitution,
            Map<UnsolvedKind, Kind> kindSubstitution) {
        this.typeSubstitution = typeSubstitution;
        this.kindTransformer = new KindSubstitutionTransformer(kindSubstitution);
    }

    public TypeSubstitutionTransformer(Map<UnsolvedType, MonoType> typeSubstitution) {
        this.typeSubstitution = typeSubstitution;
    }

    @Override
    public TypeLambda visitTypeLambda(TypeLambda tyLam) {
        return new TypeLambda(
                tyLam.args().collect(tyArg -> tyArg.accept(this)),
                tyLam.body().accept(this),
                tyLam.kind().accept(kindTransformer));
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
        if (typeSubstitution.containsKey(unsolved)) {
            return typeSubstitution
                    .get(unsolved)
                    .accept(this);
        } else {
            return new UnsolvedType(
                    unsolved.id(),
                    unsolved.kind().accept(kindTransformer));
        }
    }
}

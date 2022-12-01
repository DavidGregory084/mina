package org.mina_lang.common.types;

public sealed interface MonoType extends Type permits TypeConstructor, BuiltInType, TypeApply, TypeVar, UnsolvedType {
    @Override
    default Polarity polarity() {
        return Polarity.NON_POLAR;
    }

    @Override
    MonoType accept(TypeTransformer visitor);

    @Override
    default public MonoType substitute(
            UnionFind<MonoType> typeSubstitution,
            UnionFind<Kind> kindSubstitution) {
        return accept(new TypeSubstitutionTransformer(typeSubstitution, kindSubstitution));
    }

    default public MonoType defaultKinds() {
        return accept(new TypeSubstitutionTransformer(new KindDefaultingTransformer()));
    }
}

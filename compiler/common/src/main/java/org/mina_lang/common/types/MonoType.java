package org.mina_lang.common.types;

import java.util.Map;

public sealed interface MonoType extends Type permits TypeConstructor, BuiltInType, TypeApply, TypeVar, UnsolvedType {
    @Override
    default Polarity polarity() {
        return Polarity.NON_POLAR;
    }

    @Override
    MonoType accept(TypeTransformer visitor);

    @Override
    default public MonoType substitute(Map<UnsolvedType, MonoType> substitution) {
        return accept(new TypeSubstitutionTransformer(substitution));
    }

    @Override
    default public MonoType substitute(
            Map<UnsolvedType, MonoType> typeSubstitution,
            Map<UnsolvedKind, Kind> kindSubstitution) {
        return accept(new TypeSubstitutionTransformer(typeSubstitution, kindSubstitution));
    }

    default public MonoType defaultKinds() {
        return accept(new TypeSubstitutionTransformer(new KindDefaultingTransformer()));
    }
}

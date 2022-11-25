package org.mina_lang.common.types;

import java.util.Map;

public sealed interface PolyType extends Type permits TypeLambda, PropositionType, ImplicationType {
    @Override
    default Polarity polarity() {
        return Polarity.NON_POLAR;
    }

    @Override
    PolyType accept(TypeTransformer visitor);

    @Override
    default public PolyType substitute(Map<UnsolvedType, MonoType> substitution) {
        return accept(new TypeSubstitutionTransformer(substitution));
    }

    @Override
    default public PolyType substitute(
            Map<UnsolvedType, MonoType> typeSubstitution,
            Map<UnsolvedKind, Kind> kindSubstitution) {
        return accept(new TypeSubstitutionTransformer(typeSubstitution, kindSubstitution));
    }

    default public PolyType defaultKinds() {
        return accept(new TypeSubstitutionTransformer(new KindDefaultingTransformer()));
    }
}

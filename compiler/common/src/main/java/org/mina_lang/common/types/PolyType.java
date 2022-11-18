package org.mina_lang.common.types;

public sealed interface PolyType extends Type permits TypeLambda, PropositionType, ImplicationType {
    @Override
    default Polarity polarity() {
        return Polarity.NON_POLAR;
    }

    @Override
    PolyType accept(TypeTransformer visitor);
}

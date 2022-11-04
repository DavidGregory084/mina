package org.mina_lang.common.types;

public sealed interface MonoType extends Type permits TypeConstructor, TypeApply, TypeVar, UnsolvedType {
    @Override
    default Polarity polarity() {
        return Polarity.NON_POLAR;
    }

    @Override
    MonoType accept(TypeTransformer visitor);
}

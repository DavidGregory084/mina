package org.mina_lang.common.types;

public sealed interface PolyType extends Type permits ExistsType, ForAllType, PropositionType, ImplicationType {
    @Override
    PolyType accept(TypeTransformer visitor);
}

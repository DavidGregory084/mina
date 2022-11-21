package org.mina_lang.common.types;

import java.util.Map;

public sealed interface Kind extends Sort permits TypeKind, HigherKind, UnsolvedKind {
    @Override
    default <A> A accept(SortFolder<A> visitor) {
        return visitor.visitKind(this);
    }

    <A> A accept(KindFolder<A> visitor);

    Kind accept(KindTransformer visitor);

    default Kind substitute(Map<UnsolvedKind, Kind> substitution) {
        return this.accept(new KindSubstitutionTransformer(substitution));
    }
}

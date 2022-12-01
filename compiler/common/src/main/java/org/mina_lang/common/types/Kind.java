package org.mina_lang.common.types;

public sealed interface Kind extends Sort permits TypeKind, HigherKind, UnsolvedKind {
    @Override
    default <A> A accept(SortFolder<A> visitor) {
        return visitor.visitKind(this);
    }

    <A> A accept(KindFolder<A> visitor);

    Kind accept(KindTransformer visitor);

    default Kind substitute(UnionFind<Kind> substitution) {
        return accept(new KindSubstitutionTransformer(substitution));
    }

    default Kind defaultKind() {
        return accept(new KindDefaultingTransformer());
    }
}

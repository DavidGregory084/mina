package org.mina_lang.common.types;

public sealed interface Sort permits Type, Kind {
    <A> A accept(SortFolder<A> visitor);

    default Sort substitute(
            UnionFind<MonoType> typeSubstitution,
            UnionFind<Kind> kindSubstitution) {
        return accept(new SortSubstitutionTransformer(typeSubstitution, kindSubstitution));
    }
}

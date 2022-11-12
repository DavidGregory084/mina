package org.mina_lang.common.types;

public sealed interface Sort permits Type, Kind {
    <A> A accept(SortFolder<A> visitor);
}

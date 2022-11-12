package org.mina_lang.common.types;

public interface SortFolder<A> extends KindFolder<A>, TypeFolder<A> {
    default A visitSort(Sort sort) {
        return sort.accept(this);
    }
}

package org.mina_lang.common.types;

public sealed interface TypeVar extends MonoType permits ExistsVar, ForAllVar {

    @Override
    default <A> A accept(TypeFolder<A> visitor) {
        return this.accept(visitor);
    }

    @Override
    default MonoType accept(TypeTransformer visitor) {
        return this.accept(visitor);
    }
}

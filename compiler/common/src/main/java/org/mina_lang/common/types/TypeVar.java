package org.mina_lang.common.types;

public record TypeVar(String name, Kind kind) implements MonoType {

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitTypeVar(this);
    }

    @Override
    public TypeVar accept(TypeTransformer visitor) {
        return visitor.visitTypeVar(this);
    }
}

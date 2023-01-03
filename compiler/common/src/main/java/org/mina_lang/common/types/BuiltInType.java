package org.mina_lang.common.types;

public record BuiltInType(String name, Kind kind) implements MonoType {
    @Override
    public boolean isPrimitive() {
        return Type.primitives.contains(this);
    }

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitBuiltInType(this);
    }

    @Override
    public BuiltInType accept(TypeTransformer visitor) {
        return visitor.visitBuiltInType(this);
    }
}

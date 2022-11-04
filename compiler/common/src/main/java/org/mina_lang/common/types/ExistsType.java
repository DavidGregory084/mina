package org.mina_lang.common.types;

public record ExistsType(TypeVar typeVar, Type type) implements PolyType {
    @Override
    public Polarity polarity() {
        return Polarity.POSITIVE;
    }

    @Override
    public Kind kind() {
        return type.kind();
    }

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitExistsType(this);
    }

    @Override
    public ExistsType accept(TypeTransformer visitor) {
        return visitor.visitExistsType(this);
    }
}

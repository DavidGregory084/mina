package org.mina_lang.common.types;

public record ForAllType(TypeVar typeVar, Type type) implements PolyType {
    @Override
    public Polarity polarity() {
        return Polarity.NEGATIVE;
    }

    @Override
    public Kind kind() {
        return type.kind();
    }

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitForAllType(this);
    }

    @Override
    public ForAllType accept(TypeTransformer visitor) {
        return visitor.visitForAllType(this);
    }
}

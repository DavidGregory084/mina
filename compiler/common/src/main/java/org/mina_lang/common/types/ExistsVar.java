package org.mina_lang.common.types;

public record ExistsVar(String name, Kind kind) implements TypeVar {

    @Override
    public Polarity polarity() {
        return Polarity.POSITIVE;
    }

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitExistsVar(this);
    }

    @Override
    public ExistsVar accept(TypeTransformer visitor) {
        return visitor.visitExistsVar(this);
    }
}

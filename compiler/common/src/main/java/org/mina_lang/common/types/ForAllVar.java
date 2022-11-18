package org.mina_lang.common.types;

public record ForAllVar(String name, Kind kind) implements TypeVar {

    @Override
    public Polarity polarity() {
        return Polarity.NEGATIVE;
    }

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitForAllVar(this);
    }

    @Override
    public ForAllVar accept(TypeTransformer visitor) {
        return visitor.visitForAllVar(this);
    }
}

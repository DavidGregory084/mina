package org.mina_lang.common.types;

public record ImplicationType(Proposition property, Type impliedType) implements PolyType {
    @Override
    public Kind kind() {
        return impliedType.kind();
    }

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitImplicationType(this);
    }

    @Override
    public ImplicationType accept(TypeTransformer visitor) {
        return visitor.visitImplicationType(this);
    }
}

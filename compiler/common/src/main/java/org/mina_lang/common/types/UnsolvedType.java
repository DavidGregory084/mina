package org.mina_lang.common.types;

public record UnsolvedType(int id, Kind kind) implements MonoType {
    public String name() {
        var div = (id / 26) + 1;
        var rem = id % 26;
        var prefixChar = (char) ('A' + rem);
        return String.valueOf(prefixChar) + String.valueOf(div);
    }

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitUnsolvedType(this);
    }

    @Override
    public MonoType accept(TypeTransformer visitor) {
        return visitor.visitUnsolvedType(this);
    }
}

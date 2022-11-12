package org.mina_lang.common.types;

public record UnsolvedKind(int id) implements Kind {
    public String name() {
        var div = (id / 26) + 1;
        var rem = id % 26;
        var prefixChar = (char) ('A' + rem);
        return String.valueOf(prefixChar) + String.valueOf(div);
    }

    @Override
    public <A> A accept(KindFolder<A> visitor) {
        return visitor.visitUnsolvedKind(this);
    }

    @Override
    public Kind accept(KindTransformer visitor) {
        return visitor.visitUnsolvedKind(this);
    }
}

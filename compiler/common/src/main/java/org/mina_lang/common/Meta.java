package org.mina_lang.common;

public record Meta<A>(Range range, A meta) {
    public static Meta<Void> EMPTY = new Meta<>(Range.EMPTY, null);

    public Meta<A> withRange(Range range) {
        return new Meta<A>(range, meta());
    }

    public Meta<A> withMeta(A meta) {
        return new Meta<A>(range(), meta);
    }

    public static Meta<Void> of(Range range) {
        return new Meta<Void>(range, null);
    }

    public static <A> Meta<A> of(A meta) {
        return new Meta<A>(Range.EMPTY, meta);
    }
}

package org.mina_lang.runtime;

@FunctionalInterface
public interface Function1<A0, R> extends Function<R> {
    public R apply(A0 arg0);
}

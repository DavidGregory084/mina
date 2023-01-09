package org.mina_lang.runtime;

@FunctionalInterface
public interface Function0<R> extends Function<R> {
    public R apply();
}

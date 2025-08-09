/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common;

import org.mina_lang.common.functions.TriConsumer;
import org.mina_lang.common.names.ConstructorName;

import java.util.Deque;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Represents a name or type environment as a stack of scopes,
 * which are searched innermost to outermost when resolving names.
 * <p>
 * Inspired by the "scopes" and "ribs" of the Rust compiler naming phase as
 * described in the
 * <a href="https://rustc-dev-guide.rust-lang.org/name-resolution.html">Guide To
 * Rustc Development</a>.
 */
public interface Environment<A, B extends Scope<A>> {

    Deque<B> scopes();

    default B topScope() {
        return scopes().peek();
    }

    default Optional<Meta<A>> lookupValue(String name) {
        return scopes().stream()
            .filter(scope -> scope.hasValue(name))
            .findFirst()
            .flatMap(scope -> scope.lookupValue(name));
    }

    default <C> Optional<Meta<A>> lookupValueOrElse(String name, Meta<C> meta,
            BiConsumer<String, Meta<C>> orElseFn) {
        var valueMeta = lookupValue(name);

        if (valueMeta.isEmpty()) {
            orElseFn.accept(name, meta);
        }

        return valueMeta;
    }

    default void putValue(String name, Meta<A> meta) {
        scopes().stream()
            .findFirst()
            .ifPresent(scope -> scope.putValue(name, meta));
    }

    default Meta<A> putValueIfAbsent(String name, Meta<A> meta) {
        return lookupValue(name)
                .orElseGet(() -> {
                    return scopes().stream()
                            .findFirst()
                            .map(scope -> scope.putValueIfAbsent(name, meta))
                            .orElse(null);
                });
    }

    default void putValueIfAbsentOrElse(String name, Meta<A> proposed,
            TriConsumer<String, Meta<A>, Meta<A>> orElseFn) {
        var existing = putValueIfAbsent(name, proposed);
        if (existing != null) {
            orElseFn.accept(name, proposed, existing);
        }
    }

    default Optional<Meta<A>> lookupType(String name) {
        return scopes().stream()
            .filter(scope -> scope.hasType(name))
            .findFirst()
            .flatMap(scope -> scope.lookupType(name));
    }

    default <C> Optional<Meta<A>> lookupTypeOrElse(String name, Meta<C> meta,
            BiConsumer<String, Meta<C>> orElseFn) {
        var typeMeta = lookupType(name);

        if (typeMeta.isEmpty()) {
            orElseFn.accept(name, meta);
        }

        return typeMeta;
    };

    default void putType(String name, Meta<A> meta) {
        scopes().stream()
            .findFirst()
            .ifPresent(scope -> scope.putType(name, meta));
    }

    default Meta<A> putTypeIfAbsent(String name, Meta<A> meta) {
        return lookupType(name)
                .orElseGet(() -> {
                    return scopes().stream()
                            .findFirst()
                            .map(scope -> scope.putTypeIfAbsent(name, meta))
                            .orElse(null);
                });
    }

    default void putTypeIfAbsentOrElse(String name, Meta<A> proposed,
            TriConsumer<String, Meta<A>, Meta<A>> orElseFn) {
        var existing = putTypeIfAbsent(name, proposed);
        if (existing != null) {
            orElseFn.accept(name, proposed, existing);
        }
    }

    default Optional<Meta<A>> lookupField(ConstructorName constr, String name) {
        return scopes().stream()
            .filter(scope -> scope.hasField(constr, name))
            .findFirst()
            .flatMap(scope -> scope.lookupField(constr, name));
    }

    default <C> Optional<Meta<A>> lookupFieldOrElse(ConstructorName constr, String name, Meta<C> meta,
            BiConsumer<String, Meta<C>> orElseFn) {
        var fieldMeta = lookupField(constr, name);

        if (fieldMeta.isEmpty()) {
            orElseFn.accept(name, meta);
        }

        return fieldMeta;
    }

    default void putField(ConstructorName constr, String name, Meta<A> meta) {
        scopes().stream()
            .findFirst()
            .ifPresent(scope -> scope.putField(constr, name, meta));
    }

    default Meta<A> putFieldIfAbsent(ConstructorName constr, String name, Meta<A> meta) {
        return lookupField(constr, name)
                .orElseGet(() -> {
                    return scopes().stream()
                            .findFirst()
                            .map(scope -> scope.putFieldIfAbsent(constr, name, meta))
                            .orElse(null);
                });
    }

    default void putFieldIfAbsentOrElse(ConstructorName constr, String name, Meta<A> proposed,
            TriConsumer<String, Meta<A>, Meta<A>> orElseFn) {
        var existing = putFieldIfAbsent(constr, name, proposed);
        if (existing != null) {
            orElseFn.accept(name, proposed, existing);
        }
    }

    default void pushScope(B scope) {
        scopes().push(scope);
    }

    default void popScope(Class<?> expected) {
        var poppedScope = scopes().pop();
        assert expected.isAssignableFrom(poppedScope.getClass());
    }
}

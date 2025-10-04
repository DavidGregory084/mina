/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.mina_lang.common.functions.TriConsumer;
import org.mina_lang.common.names.ConstructorName;

import java.util.Optional;
import java.util.function.BiConsumer;

public interface Scope<A> {
    MutableMap<String, A> values();

    MutableMap<String, A> types();

    MutableMap<ConstructorName, MutableMap<String, A>> fields();

    default boolean hasValue(String name) {
        return values().containsKey(name);
    }

    default Optional<A> lookupValue(String name) {
        return Optional.ofNullable(values().get(name));
    }

    default <B> Optional<A> lookupValueOrElse(String name, B meta, BiConsumer<String, B> orElseFn) {
        var valueMeta = lookupValue(name);
        if (valueMeta.isEmpty()) {
            orElseFn.accept(name, meta);
        }
        return valueMeta;
    }

    default void putValue(String name, A meta) {
        values().put(name, meta);
    }

    default A putValueIfAbsent(String name, A meta) {
        return values().putIfAbsent(name, meta);
    }

    default void putValueIfAbsentOrElse(String name, A proposed,
            TriConsumer<String, A, A> orElseFn) {
        var existing = putValueIfAbsent(name, proposed);
        if (existing != null) {
            orElseFn.accept(name, proposed, existing);
        }
    }

    default boolean hasType(String name) {
        return types().containsKey(name);
    }

    default Optional<A> lookupType(String name) {
        return Optional.ofNullable(types().get(name));
    }

    default <B> Optional<A> lookupTypeOrElse(String name, B meta, BiConsumer<String, B> orElseFn) {
        var typeMeta = lookupType(name);
        if (typeMeta.isEmpty()) {
            orElseFn.accept(name, meta);
        }
        return typeMeta;
    }

    default void putType(String name, A meta) {
        types().put(name, meta);
    }

    default A putTypeIfAbsent(String name, A meta) {
        return types().putIfAbsent(name, meta);
    }

    default void putTypeIfAbsentOrElse(String name, A proposed,
            TriConsumer<String, A, A> orElseFn) {
        var existing = putTypeIfAbsent(name, proposed);
        if (existing != null) {
            orElseFn.accept(name, proposed, existing);
        }
    }

    default boolean hasField(ConstructorName constr, String field) {
        return Optional.ofNullable(fields().get(constr))
                .map(fields -> fields.containsKey(field))
                .orElse(false);
    }

    default Optional<A> lookupField(ConstructorName constr, String name) {
        return Optional.ofNullable(fields().get(constr))
                .map(fields -> fields.get(name));
    }

    default <B> Optional<A> lookupFieldOrElse(ConstructorName constr, String name, B meta,
            BiConsumer<String, B> orElseFn) {
        var fieldMeta = lookupField(constr, name);
        if (fieldMeta.isEmpty()) {
            orElseFn.accept(name, meta);
        }
        return fieldMeta;
    }

    default void putField(ConstructorName constr, String name, A meta) {
        var constrFields = fields().getIfAbsentPut(constr, Maps.mutable::empty);
        constrFields.put(name, meta);
    }

    default A putFieldIfAbsent(ConstructorName constr, String name, A meta) {
        var constrFields = fields().getIfAbsentPut(constr, Maps.mutable::empty);
        return constrFields.putIfAbsent(name, meta);
    }

    default void putFieldIfAbsentOrElse(ConstructorName constr, String name, A proposed,
            TriConsumer<String, A, A> orElseFn) {
        var existing = putFieldIfAbsent(constr, name, proposed);
        if (existing != null) {
            orElseFn.accept(name, proposed, existing);
        }
    }
}

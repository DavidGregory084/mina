/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common;

import org.eclipse.collections.api.block.function.Function3;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.mina_lang.common.names.ConstructorName;

import java.util.Optional;
import java.util.function.BiConsumer;

public interface Scope<A> {
    MutableMap<String, Meta<A>> values();

    MutableMap<String, Meta<A>> types();

    MutableMap<ConstructorName, MutableMap<String, Meta<A>>> fields();

    default boolean hasValue(String name) {
        return values().containsKey(name);
    }

    default Optional<Meta<A>> lookupValue(String name) {
        return Optional.ofNullable(values().get(name));
    };

    default <B> Optional<Meta<A>> lookupValueOrElse(String name, Meta<B> meta, BiConsumer<String, Meta<B>> orElseFn) {
        var valueMeta = lookupValue(name);
        if (valueMeta.isEmpty()) {
            orElseFn.accept(name, meta);
        }
        return valueMeta;
    };

    default void putValue(String name, Meta<A> meta) {
        values().put(name, meta);
    }

    default Meta<A> putValueIfAbsent(String name, Meta<A> meta) {
        return values().putIfAbsent(name, meta);
    }

    default void putValueIfAbsentOrElse(String name, Meta<A> proposed,
            Function3<String, Meta<A>, Meta<A>, Void> orElseFn) {
        var existing = putValueIfAbsent(name, proposed);
        if (existing != null) {
            orElseFn.value(name, proposed, existing);
        }
    }

    default boolean hasType(String name) {
        return types().containsKey(name);
    }

    default Optional<Meta<A>> lookupType(String name) {
        return Optional.ofNullable(types().get(name));
    }

    default <B> Optional<Meta<A>> lookupTypeOrElse(String name, Meta<B> meta, BiConsumer<String, Meta<B>> orElseFn) {
        var typeMeta = lookupType(name);
        if (typeMeta.isEmpty()) {
            orElseFn.accept(name, meta);
        }
        return typeMeta;
    };

    default void putType(String name, Meta<A> meta) {
        types().put(name, meta);
    }

    default Meta<A> putTypeIfAbsent(String name, Meta<A> meta) {
        return types().putIfAbsent(name, meta);
    }

    default void putTypeIfAbsentOrElse(String name, Meta<A> proposed,
            Function3<String, Meta<A>, Meta<A>, Void> orElseFn) {
        var existing = putTypeIfAbsent(name, proposed);
        if (existing != null) {
            orElseFn.value(name, proposed, existing);
        }
    };

    default boolean hasField(ConstructorName constr, String field) {
        return Optional.ofNullable(fields().get(constr))
                .map(fields -> fields.containsKey(field))
                .orElse(false);
    }

    default Optional<Meta<A>> lookupField(ConstructorName constr, String name) {
        return Optional.ofNullable(fields().get(constr))
                .map(fields -> fields.get(name));
    };

    default <B> Optional<Meta<A>> lookupFieldOrElse(ConstructorName constr, String name, Meta<B> meta,
            BiConsumer<String, Meta<B>> orElseFn) {
        var fieldMeta = lookupField(constr, name);
        if (fieldMeta.isEmpty()) {
            orElseFn.accept(name, meta);
        }
        return fieldMeta;
    };

    default void putField(ConstructorName constr, String name, Meta<A> meta) {
        var constrFields = fields().getIfAbsentPut(constr, () -> Maps.mutable.empty());
        constrFields.put(name, meta);
    }

    default Meta<A> putFieldIfAbsent(ConstructorName constr, String name, Meta<A> meta) {
        var constrFields = fields().getIfAbsentPut(constr, () -> Maps.mutable.empty());
        return constrFields.putIfAbsent(name, meta);
    }

    default void putFieldIfAbsentOrElse(ConstructorName constr, String name, Meta<A> proposed,
            Function3<String, Meta<A>, Meta<A>, Void> orElseFn) {
        var existing = putFieldIfAbsent(constr, name, proposed);
        if (existing != null) {
            orElseFn.value(name, proposed, existing);
        }
    }
}

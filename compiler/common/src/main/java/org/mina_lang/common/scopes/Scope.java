package org.mina_lang.common.scopes;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.eclipse.collections.api.block.function.Function3;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.Meta;

public sealed interface Scope<A> permits BuiltInScope, ImportedScope, NamespaceScope, DataScope, ConstructorScope, BlockScope, LambdaScope, CaseScope, ConstructorPatternScope, TypeLambdaScope {
    MutableMap<String, Meta<A>> values();

    MutableMap<String, Meta<A>> types();

    MutableMap<ConstructorName, MutableMap<String, Meta<A>>> constructorFields();

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

    default boolean populateValue(String name, Meta<A> meta) {
        return values().putIfAbsent(name, meta) == null;
    }

    default void populateValueOrElse(String name, Meta<A> proposed,
            Function3<String, Meta<A>, Meta<A>, Void> orElseFn) {
        if (!populateValue(name, proposed)) {
            lookupValue(name).ifPresent(existing -> orElseFn.value(name, proposed, existing));
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

    default boolean populateType(String name, Meta<A> meta) {
        return types().putIfAbsent(name, meta) == null;
    }

    default void populateTypeOrElse(String name, Meta<A> proposed,
            Function3<String, Meta<A>, Meta<A>, Void> orElseFn) {
        if (!populateType(name, proposed)) {
            lookupType(name).ifPresent(existing -> orElseFn.value(name, proposed, existing));
        }
    };

    default boolean hasField(ConstructorName constr, String field) {
        return Optional.ofNullable(constructorFields().get(constr))
                .map(fields -> fields.containsKey(field))
                .orElse(false);
    }

    default Optional<Meta<A>> lookupField(ConstructorName constr, String name) {
        return Optional.ofNullable(constructorFields().get(constr))
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

    default boolean populateField(ConstructorName constr, String name, Meta<A> meta) {
        var constrFields = constructorFields().getIfAbsentPut(constr, () -> Maps.mutable.empty());
        var putResult = constrFields.putIfAbsent(name, meta);
        return putResult == null;
    }

    default void populateFieldOrElse(ConstructorName constr, String name, Meta<A> proposed,
            Function3<String, Meta<A>, Meta<A>, Void> orElseFn) {
        if (!populateField(constr, name, proposed)) {
            lookupField(constr, name).ifPresent(existing -> orElseFn.value(name, proposed, existing));
        }
    }
}

package org.mina_lang.common;

import java.util.Optional;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;

public record NameEnvironment(MutableMap<String, Meta<Name>> names) {

    public Meta<Name> get(String name) {
        return names().get(name);
    }

    public Optional<Meta<Name>> getOptional(String name) {
        return Optional.ofNullable(get(name));
    }

    public void put(String name, Meta<Name> meta) {
        names().put(name, meta);
    }

    public static NameEnvironment empty() {
        return new NameEnvironment(Maps.mutable.empty());
    }
}

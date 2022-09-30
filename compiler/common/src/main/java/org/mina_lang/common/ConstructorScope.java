package org.mina_lang.common;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;

public record ConstructorScope<A> (ConstructorName constr, MutableMap<String, Meta<A>> values,
        MutableMap<String, Meta<A>> types, MutableMap<ConstructorName, MutableMap<String, Meta<A>>> constructorFields) implements Scope<A> {

    public ConstructorScope(ConstructorName constr) {
        this(constr, Maps.mutable.empty(), Maps.mutable.empty(), Maps.mutable.empty());
    }
}

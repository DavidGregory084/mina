package org.mina_lang.common;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;

public record CaseScope<A>(MutableMap<String, Meta<A>> values, MutableMap<String, Meta<A>> types, MutableMap<ConstructorName, MutableMap<String, Meta<A>>> constructorFields) implements Scope<A> {
    public CaseScope() {
        this(Maps.mutable.empty(), Maps.mutable.empty(), Maps.mutable.empty());
    }
}

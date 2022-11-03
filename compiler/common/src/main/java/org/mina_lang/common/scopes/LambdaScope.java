package org.mina_lang.common.scopes;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.Meta;

public record LambdaScope<A>(MutableMap<String, Meta<A>> values, MutableMap<String, Meta<A>> types, MutableMap<ConstructorName, MutableMap<String, Meta<A>>> constructorFields) implements Scope<A> {
    public LambdaScope() {
        this(Maps.mutable.empty(), Maps.mutable.empty(), Maps.mutable.empty());
    }
}

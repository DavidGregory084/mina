package org.mina_lang.common.scopes;

import java.util.Optional;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.Meta;

public record ConstructorPatternScope<A> (Optional<ConstructorName> constr, MutableMap<String, Meta<A>> values,
                                          MutableMap<String, Meta<A>> types, MutableMap<ConstructorName, MutableMap<String, Meta<A>>> constructorFields) implements Scope<A> {

    public ConstructorPatternScope(Optional<ConstructorName> constr) {
        this(constr, Maps.mutable.empty(), Maps.mutable.empty(), Maps.mutable.empty());
    }

    public ConstructorPatternScope(ConstructorName constr) {
        this(Optional.ofNullable(constr), Maps.mutable.empty(), Maps.mutable.empty(), Maps.mutable.empty());
    }
}

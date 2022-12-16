package org.mina_lang.common.scopes;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.MutableSet;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.types.UnsolvedKind;
import org.mina_lang.common.types.UnsolvedType;
import org.mina_lang.common.Meta;

public record LambdaScope<A>(
        MutableMap<String, Meta<A>> values,
        MutableMap<String, Meta<A>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<A>>> constructorFields,
        MutableSet<UnsolvedKind> unsolvedKinds,
        MutableSet<UnsolvedType> unsolvedTypes) implements Scope<A> {
    public LambdaScope() {
        this(
            Maps.mutable.empty(),
            Maps.mutable.empty(),
            Maps.mutable.empty(),
            Sets.mutable.empty(),
            Sets.mutable.empty());
    }
}

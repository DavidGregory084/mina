package org.mina_lang.common.scopes;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Range;
import org.mina_lang.common.names.BuiltInName;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.types.BuiltInType;
import org.mina_lang.common.types.Type;

public record BuiltInScope<A>(MutableMap<String, Meta<A>> values, MutableMap<String, Meta<A>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<A>>> constructorFields) implements Scope<A> {
    public BuiltInScope() {
        this(Maps.mutable.empty(), Maps.mutable.empty(), Maps.mutable.empty());
    }

    public static BuiltInScope<Name> withBuiltInNames() {
        var builtIns = Type.builtIns
                .collect(BuiltInType::name)
                .toMap(
                        name -> name,
                        name -> new Meta<Name>(Range.EMPTY, new BuiltInName(name)));

        return new BuiltInScope<>(Maps.mutable.empty(), builtIns, Maps.mutable.empty());
    }

    public static BuiltInScope<Attributes> withBuiltInTypes() {
        var builtIns = Type.builtIns
                .toMap(
                        typ -> typ.name(),
                        typ -> new Meta<>(Range.EMPTY, new Attributes(new BuiltInName(typ.name()), typ.kind())));

        return new BuiltInScope<>(Maps.mutable.empty(), builtIns, Maps.mutable.empty());
    }
}

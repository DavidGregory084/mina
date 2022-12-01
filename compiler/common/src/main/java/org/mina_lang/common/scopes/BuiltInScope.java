package org.mina_lang.common.scopes;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.tuple.Tuples;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Range;
import org.mina_lang.common.names.BuiltInName;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.types.BuiltInType;
import org.mina_lang.common.types.Type;
import org.mina_lang.common.types.UnsolvedKind;
import org.mina_lang.common.types.UnsolvedType;

public record BuiltInScope<A>(
        MutableMap<String, Meta<A>> values,
        MutableMap<String, Meta<A>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<A>>> constructorFields,
        MutableSet<UnsolvedKind> unsolvedKinds,
        MutableSet<UnsolvedType> unsolvedTypes) implements Scope<A> {
    public BuiltInScope() {
        this(
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Sets.mutable.empty(),
                Sets.mutable.empty());
    }

    public static BuiltInScope<Name> withBuiltInNames() {
        var builtInNames = Type.builtIns
                .toMap(
                        typ -> typ.name(),
                        typ -> Meta.<Name>of(new BuiltInName(typ.name())));

        return new BuiltInScope<>(
                Maps.mutable.empty(),
                builtInNames,
                Maps.mutable.empty(),
                Sets.mutable.empty(),
                Sets.mutable.empty());
    }

    public static BuiltInScope<Attributes> withBuiltInTypes() {
        var builtInTypes = Type.builtIns
                .toMap(
                        typ -> typ.name(),
                        typ -> Meta.of(new Attributes(new BuiltInName(typ.name()), typ.kind())));

        return new BuiltInScope<>(
                Maps.mutable.empty(),
                builtInTypes,
                Maps.mutable.empty(),
                Sets.mutable.empty(),
                Sets.mutable.empty());
    }
}

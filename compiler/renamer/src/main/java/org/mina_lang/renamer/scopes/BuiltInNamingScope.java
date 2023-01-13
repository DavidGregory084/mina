package org.mina_lang.renamer.scopes;

import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.BuiltInName;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.types.Type;

public record BuiltInNamingScope(
        MutableMap<String, Meta<Name>> values,
        MutableMap<String, Meta<Name>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<Name>>> fields) implements NamingScope {
    public BuiltInNamingScope() {
        this(
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty());
    }

    public static BuiltInNamingScope empty() {
        var builtInNames = Type.builtIns
                .toMap(
                        typ -> typ.name(),
                        typ -> Meta.<Name>of(new BuiltInName(typ.name())));

        return new BuiltInNamingScope(
                Maps.mutable.empty(),
                builtInNames,
                Maps.mutable.empty());
    }
}

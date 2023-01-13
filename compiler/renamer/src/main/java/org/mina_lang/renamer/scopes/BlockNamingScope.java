package org.mina_lang.renamer.scopes;

import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.Name;

public record BlockNamingScope(
        MutableMap<String, Meta<Name>> values,
        MutableMap<String, Meta<Name>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<Name>>> fields) implements NamingScope {
    public BlockNamingScope() {
        this(
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty());
    }
}

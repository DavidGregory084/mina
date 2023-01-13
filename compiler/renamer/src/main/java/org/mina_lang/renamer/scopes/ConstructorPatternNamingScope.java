package org.mina_lang.renamer.scopes;

import java.util.Optional;

import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.Sets;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.MutableSet;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.types.UnsolvedKind;
import org.mina_lang.common.types.UnsolvedType;

public record ConstructorPatternNamingScope(
        Optional<ConstructorName> constr,
        MutableMap<String, Meta<Name>> values,
        MutableMap<String, Meta<Name>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<Name>>> fields,
        MutableSet<UnsolvedKind> unsolvedKinds,
        MutableSet<UnsolvedType> unsolvedTypes) implements NamingScope {

    public ConstructorPatternNamingScope(Optional<ConstructorName> constr) {
        this(
                constr,
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Sets.mutable.empty(),
                Sets.mutable.empty());
    }

    public ConstructorPatternNamingScope(ConstructorName constr) {
        this(
                Optional.ofNullable(constr),
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Sets.mutable.empty(),
                Sets.mutable.empty());
    }
}

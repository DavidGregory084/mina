package org.mina_lang.common.scopes.typing;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.MutableSet;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.BuiltInName;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.scopes.TypingScope;
import org.mina_lang.common.types.Type;
import org.mina_lang.common.types.UnsolvedKind;
import org.mina_lang.common.types.UnsolvedType;

public record BuiltInTypingScope(
        MutableMap<String, Meta<Attributes>> values,
        MutableMap<String, Meta<Attributes>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<Attributes>>> fields,
        MutableSet<UnsolvedKind> unsolvedKinds,
        MutableSet<UnsolvedType> unsolvedTypes) implements TypingScope {
    public BuiltInTypingScope() {
        this(
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Sets.mutable.empty(),
                Sets.mutable.empty());
    }

    public static BuiltInTypingScope empty() {
        var builtInTypes = Type.builtIns
                .toMap(
                        typ -> typ.name(),
                        typ -> Meta.of(new Attributes(new BuiltInName(typ.name()), typ.kind())));

        return new BuiltInTypingScope(
                Maps.mutable.empty(),
                builtInTypes,
                Maps.mutable.empty(),
                Sets.mutable.empty(),
                Sets.mutable.empty());
    }
}

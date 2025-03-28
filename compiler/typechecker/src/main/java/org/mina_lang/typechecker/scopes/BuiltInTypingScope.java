/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.typechecker.scopes;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.Sets;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.BuiltInName;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.types.SyntheticVar;
import org.mina_lang.common.types.Type;
import org.mina_lang.common.types.UnsolvedKind;
import org.mina_lang.common.types.UnsolvedType;

public record BuiltInTypingScope(
        MutableMap<String, Meta<Attributes>> values,
        MutableMap<String, Meta<Attributes>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<Attributes>>> fields,
        MutableSet<SyntheticVar> syntheticVars,
        MutableSet<UnsolvedKind> unsolvedKinds,
        MutableSet<UnsolvedType> unsolvedTypes) implements TypingScope {
    public BuiltInTypingScope() {
        this(
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Sets.mutable.empty(),
                Sets.mutable.empty(),
                Sets.mutable.empty());
    }

    public static BuiltInTypingScope empty() {
        var builtInTypes = Type.builtIns
                .toMap(
                        typ -> typ.name(),
                        typ -> Meta.of(new BuiltInName(typ.name()), typ.kind()));

        return new BuiltInTypingScope(
                Maps.mutable.empty(),
                builtInTypes,
                Maps.mutable.empty(),
                Sets.mutable.empty(),
                Sets.mutable.empty(),
                Sets.mutable.empty());
    }
}

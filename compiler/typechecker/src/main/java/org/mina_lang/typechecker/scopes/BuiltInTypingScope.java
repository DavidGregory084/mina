/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.typechecker.scopes;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.BuiltInName;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.types.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record BuiltInTypingScope(
        Map<String, Meta<Attributes>> values,
        Map<String, Meta<Attributes>> types,
        Map<ConstructorName, Map<String, Meta<Attributes>>> fields,
        Set<SyntheticVar> syntheticVars,
        Set<UnsolvedKind> unsolvedKinds,
        Set<UnsolvedType> unsolvedTypes) implements TypingScope {
    public BuiltInTypingScope() {
        this(
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>());
    }

    public static BuiltInTypingScope empty() {
        var builtInTypes = Type.builtIns.values().stream()
            .collect(Collectors.toMap(
                BuiltInType::name,
                typ -> Meta.of(new BuiltInName(typ.name()), typ.kind())));

        return new BuiltInTypingScope(
                new HashMap<>(),
                builtInTypes,
                new HashMap<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>());
    }
}

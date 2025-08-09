/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.renamer.scopes;

import org.mina_lang.common.Meta;
import org.mina_lang.common.names.BuiltInName;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.types.BuiltInType;
import org.mina_lang.common.types.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record BuiltInNamingScope(
        Map<String, Meta<Name>> values,
        Map<String, Meta<Name>> types,
        Map<ConstructorName, Map<String, Meta<Name>>> fields) implements NamingScope {
    public BuiltInNamingScope() {
        this(
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>());
    }

    public static BuiltInNamingScope empty() {
        var builtInNames = Type.builtIns.values().stream()
            .collect(Collectors.toMap(
                BuiltInType::name,
                typ -> Meta.<Name>of(new BuiltInName(typ.name()))));

        return new BuiltInNamingScope(
                new HashMap<>(),
                builtInNames,
                new HashMap<>());
    }
}

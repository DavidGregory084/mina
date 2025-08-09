/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.renamer.scopes;

import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.types.UnsolvedKind;
import org.mina_lang.common.types.UnsolvedType;

import java.util.*;

public record ConstructorPatternNamingScope(
        Optional<ConstructorName> constr,
        Map<String, Meta<Name>> values,
        Map<String, Meta<Name>> types,
        Map<ConstructorName, Map<String, Meta<Name>>> fields,
        Set<UnsolvedKind> unsolvedKinds,
        Set<UnsolvedType> unsolvedTypes) implements NamingScope {

    public ConstructorPatternNamingScope(Optional<ConstructorName> constr) {
        this(
                constr,
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashSet<>(),
                new HashSet<>());
    }

    public ConstructorPatternNamingScope(ConstructorName constr) {
        this(
                Optional.ofNullable(constr),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashSet<>(),
                new HashSet<>());
    }
}

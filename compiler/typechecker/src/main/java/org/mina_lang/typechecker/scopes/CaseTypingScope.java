/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.typechecker.scopes;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.types.SyntheticVar;
import org.mina_lang.common.types.UnsolvedKind;
import org.mina_lang.common.types.UnsolvedType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record CaseTypingScope(
        Map<String, Meta<Attributes>> values,
        Map<String, Meta<Attributes>> types,
        Map<ConstructorName, Map<String, Meta<Attributes>>> fields,
        Set<SyntheticVar> syntheticVars,
        Set<UnsolvedKind> unsolvedKinds,
        Set<UnsolvedType> unsolvedTypes) implements TypingScope {
    public CaseTypingScope() {
        this(
            new HashMap<>(),
            new HashMap<>(),
            new HashMap<>(),
            new HashSet<>(),
            new HashSet<>(),
            new HashSet<>());
    }
}

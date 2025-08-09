/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.renamer.scopes;

import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.names.NamespaceName;

import java.util.HashMap;
import java.util.Map;

public record NamespaceNamingScope(
        NamespaceName namespace,
        Map<String, Meta<Name>> values,
        Map<String, Meta<Name>> types,
        Map<ConstructorName, Map<String, Meta<Name>>> fields)
        implements NamingScope {

    public NamespaceNamingScope(NamespaceName namespace) {
        this(
                namespace,
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>());
    }
}

/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.renamer.scopes;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.DeclarationName;
import org.mina_lang.common.names.LetName;
import org.mina_lang.common.names.Name;

public record LetNamingScope(
        LetName let,
        MutableMap<String, Meta<Name>> values,
        MutableMap<String, Meta<Name>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<Name>>> fields) implements DeclarationNamingScope {

    public LetNamingScope(LetName let) {
        this(
                let,
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty());
    }

    @Override
    public DeclarationName declarationName() {
        return let;
    }
}

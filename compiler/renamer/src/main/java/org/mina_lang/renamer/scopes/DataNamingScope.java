/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.renamer.scopes;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.DataName;
import org.mina_lang.common.names.Name;

public record DataNamingScope(
        DataName data,
        MutableMap<String, Meta<Name>> values,
        MutableMap<String, Meta<Name>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<Name>>> fields) implements DeclarationNamingScope {
    public DataNamingScope(DataName data) {
        this(
                data,
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty());
    }

    @Override
    public DataName declarationName() {
        return data;
    }
}

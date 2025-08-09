/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.testing;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Scope;
import org.mina_lang.common.names.ConstructorName;

import java.util.HashMap;
import java.util.Map;

public record GenScope(
    Map<String, Meta<Attributes>> values,
    Map<String, Meta<Attributes>> types,
    Map<ConstructorName, Map<String, Meta<Attributes>>> fields
) implements Scope<Attributes> {
    public GenScope() {
        this(new HashMap<>(), new HashMap<>(), new HashMap<>());
    }
}

/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.testing;

import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Scope;
import org.mina_lang.common.names.ConstructorName;

public record GenScope(
    MutableMap<String, Meta<Attributes>> values,
    MutableMap<String, Meta<Attributes>> types,
    MutableMap<ConstructorName, MutableMap<String, Meta<Attributes>>> fields
) implements Scope<Attributes> {}

/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common;

import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.common.names.ConstructorName;

public record TopLevelScope<A>(
    MutableMap<String, Meta<A>> values,
    MutableMap<String, Meta<A>> types,
    MutableMap<ConstructorName, MutableMap<String, Meta<A>>> fields
) implements Scope<Meta<A>> {
}

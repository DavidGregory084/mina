/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common;

import org.mina_lang.common.names.ConstructorName;

import java.util.Map;

public record TopLevelScope<A>(
    Map<String, Meta<A>> values,
    Map<String, Meta<A>> types,
    Map<ConstructorName, Map<String, Meta<A>>> fields
) implements Scope<A> {
}

/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.names.LetName;
import org.mina_lang.common.types.Type;

public record Let(LetName name, Type type, Expression body) implements Declaration {
}

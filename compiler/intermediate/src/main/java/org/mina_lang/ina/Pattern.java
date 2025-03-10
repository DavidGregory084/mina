/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.types.Type;

public sealed interface Pattern extends InaNode permits AliasPattern, ConstructorPattern, IdPattern, LiteralPattern {
    Type type();
}

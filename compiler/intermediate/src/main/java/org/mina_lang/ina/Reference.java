/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.names.QualifiedName;
import org.mina_lang.common.types.Type;

public record Reference(Type type, QualifiedName name) implements Value {
}

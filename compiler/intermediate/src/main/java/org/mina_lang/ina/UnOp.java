/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.operators.UnaryOp;
import org.mina_lang.common.types.Type;

public record UnOp(Type type, UnaryOp operator, Value expr) implements Expression {
}

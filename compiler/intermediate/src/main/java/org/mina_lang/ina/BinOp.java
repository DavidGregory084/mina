/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.operators.BinaryOp;
import org.mina_lang.common.types.Type;

public record BinOp(Type type, Value left, BinaryOp operator, Value right) implements Expression {
}

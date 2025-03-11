/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

public sealed interface Primitive extends Literal permits Boolean, Char, Double, Float, Int, Long {
    Object boxedValue();
}

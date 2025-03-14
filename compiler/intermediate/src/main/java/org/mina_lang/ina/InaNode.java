/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

public sealed interface InaNode permits Case, Constructor, Declaration, Expression, Field, FieldPattern, LocalBinding, Namespace, Param, Pattern {
    <A> A accept(InaNodeFolder<A> visitor);
}

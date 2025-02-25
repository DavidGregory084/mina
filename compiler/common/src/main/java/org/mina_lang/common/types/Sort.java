/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public sealed interface Sort permits Type, Kind {
    <A> A accept(SortFolder<A> visitor);
    void accept(SortVisitor visitor);
}

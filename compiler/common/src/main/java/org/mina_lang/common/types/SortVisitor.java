/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public interface SortVisitor extends TypeVisitor, KindVisitor {
    default void visitSort(Sort sort) {
        sort.accept(this);
    }
}

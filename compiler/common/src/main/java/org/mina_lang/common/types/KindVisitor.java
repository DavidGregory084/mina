/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public interface KindVisitor {
    default void visitKind(Kind kind) {
        kind.accept(this);
    }

    void visitTypeKind(TypeKind typ);

    void visitUnsolvedKind(UnsolvedKind unsolved);

    void visitHigherKind(HigherKind higher);
}

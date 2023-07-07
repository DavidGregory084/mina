/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public interface KindFolder<A> {
    default A visitKind(Kind kind) {
        return kind.accept(this);
    }

    A visitTypeKind(TypeKind typ);

    A visitUnsolvedKind(UnsolvedKind unsolved);

    A visitHigherKind(HigherKind higher);
}

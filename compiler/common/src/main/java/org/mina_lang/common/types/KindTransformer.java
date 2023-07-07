/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public interface KindTransformer {
    default Kind visitKind(Kind kind) {
        return kind.accept(this);
    }

    TypeKind visitTypeKind(TypeKind typ);

    Kind visitUnsolvedKind(UnsolvedKind unsolved);

    HigherKind visitHigherKind(HigherKind higher);
}

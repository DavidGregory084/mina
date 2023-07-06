/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public sealed interface Kind extends Sort permits TypeKind, HigherKind, UnsolvedKind {
    @Override
    default <A> A accept(SortFolder<A> visitor) {
        return visitor.visitKind(this);
    }

    <A> A accept(KindFolder<A> visitor);

    Kind accept(KindTransformer visitor);
}

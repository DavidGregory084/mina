/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public sealed interface Kind extends Sort permits TypeKind, HigherKind, UnsolvedKind {
    @Override
    default <A> A accept(SortFolder<A> visitor) {
        return visitor.visitKind(this);
    }

    @Override
    default void accept(SortVisitor visitor) {
        visitor.visitKind(this);
    }

    <A> A accept(KindFolder<A> visitor);

    void accept(KindVisitor visitor);

    Kind accept(KindTransformer visitor);
}

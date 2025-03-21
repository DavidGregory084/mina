/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public sealed interface TypeVar extends MonoType permits ExistsVar, ForAllVar, SyntheticVar {

    String name();

    @Override
    default <A> A accept(TypeFolder<A> visitor) {
        return this.accept(visitor);
    }

    @Override
    default MonoType accept(TypeTransformer visitor) {
        return this.accept(visitor);
    }
}

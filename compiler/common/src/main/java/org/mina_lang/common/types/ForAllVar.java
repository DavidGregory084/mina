/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public record ForAllVar(String name, Kind kind) implements TypeVar {

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visitForAllVar(this);
    }

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitForAllVar(this);
    }

    @Override
    public MonoType accept(TypeTransformer visitor) {
        return visitor.visitForAllVar(this);
    }
}

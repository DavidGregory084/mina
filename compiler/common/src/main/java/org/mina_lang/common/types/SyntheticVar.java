/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public record SyntheticVar(int id, Kind kind) implements TypeVar {
    public String name() {
        var div = (id / 26) + 1;
        var rem = id % 26;
        var prefixChar = (char) ('A' + rem);
        return String.valueOf(prefixChar) + String.valueOf(div);
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visitSyntheticVar(this);
    }

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitSyntheticVar(this);
    }

    @Override
    public MonoType accept(TypeTransformer visitor) {
        return visitor.visitSyntheticVar(this);
    }
}

/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.types.Type;

public record Char(char value) implements Primitive {
    @Override
    public Type type() {
        return Type.CHAR;
    }

    @Override
    public Character boxedValue() {
        return value;
    }

    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitChar(value);
    }
}

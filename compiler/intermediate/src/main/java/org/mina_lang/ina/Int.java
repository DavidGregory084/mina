/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.types.Type;

public record Int(int value) implements Primitive {
    @Override
    public Type type() {
        return Type.INT;
    }

    @Override
    public Integer boxedValue() {
        return value;
    }

    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitInt(value);
    }
}

/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.types.Type;

public record Float(float value) implements Primitive {
    @Override
    public Type type() {
        return Type.FLOAT;
    }

    @Override
    public java.lang.Float boxedValue() {
        return value;
    }

    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitFloat(value);
    }
}

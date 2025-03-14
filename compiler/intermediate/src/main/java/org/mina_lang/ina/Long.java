/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.types.Type;

public record Long(long value) implements Primitive {
    @Override
    public Type type() {
        return Type.LONG;
    }

    @Override
    public java.lang.Long boxedValue() {
        return value;
    }

    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitLong(value);
    }
}

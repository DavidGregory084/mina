/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.types.Type;

public enum Unit implements Literal {
    INSTANCE;

    @Override
    public Type type() {
        return Type.UNIT;
    }

    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitUnit();
    }

    @Override
    public java.lang.String toString() {
        return "Unit";
    }
}

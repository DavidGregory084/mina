/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.types.Type;

public record Double(double value) implements Literal {
    @Override
    public Type type() {
        return Type.DOUBLE;
    }

    @Override
    public java.lang.Double boxedValue() {
        return value;
    }
}

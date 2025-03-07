/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.types.Type;

public record Boolean(boolean value) implements Literal {
    @Override
    public Type type() {
        return Type.BOOLEAN;
    }

    @Override
    public java.lang.Boolean boxedValue() {
        return value;
    }
}

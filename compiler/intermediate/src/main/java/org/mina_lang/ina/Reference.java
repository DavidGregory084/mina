/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.names.ValueName;
import org.mina_lang.common.types.Type;

public record Reference(ValueName name, Type type) implements Value {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitReference(name, type);
    }
}

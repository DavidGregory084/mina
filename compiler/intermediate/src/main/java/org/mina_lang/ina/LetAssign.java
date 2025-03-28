/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.names.LocalBindingName;
import org.mina_lang.common.types.Type;

public record LetAssign(LocalBindingName name, Type type, Expression body) implements LocalBinding {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitLetAssign(
            name, type,
            body.accept(visitor));
    }
}

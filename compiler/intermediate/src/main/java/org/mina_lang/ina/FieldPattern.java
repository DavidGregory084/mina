/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.names.FieldName;
import org.mina_lang.common.types.Type;

public record FieldPattern(FieldName name, Type type, Pattern pattern) implements InaNode {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitFieldPattern(
            name, type,
            pattern.accept(visitor));
    }
}

/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.types.Type;

public record ConstructorPattern(ConstructorName name, Type type, ImmutableList<FieldPattern> fields) implements Pattern {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitConstructorPattern(
            name, type,
            fields.collect(field -> field.accept(visitor)));
    }
}

/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.names.ConstructorName;

public record Constructor(ConstructorName name, ImmutableList<Field> fields) implements InaNode {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitConstructor(
            name,
            fields.collect(field -> field.accept(visitor)));
    }
}

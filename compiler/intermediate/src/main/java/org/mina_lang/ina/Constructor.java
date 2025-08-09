/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.names.ConstructorName;

import java.util.List;

public record Constructor(ConstructorName name, List<Field> fields) implements InaNode {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitConstructor(
            name,
            fields.stream().map(field -> field.accept(visitor)).toList());
    }
}

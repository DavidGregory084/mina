/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.types.Type;

public record If(Type type, Value condition, Expression consequent, Expression alternative) implements Expression {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitIf(
            type,
            condition.accept(visitor),
            consequent.accept(visitor),
            alternative.accept(visitor));
    }
}

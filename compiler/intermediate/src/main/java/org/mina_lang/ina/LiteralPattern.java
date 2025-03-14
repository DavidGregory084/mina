/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.types.Type;

public record LiteralPattern(Literal literal) implements Pattern {
    @Override
    public Type type() {
        return literal.type();
    }

    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitLiteralPattern(literal.accept(visitor));
    }
}

/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

public record Case(Pattern pattern, Expression consequent) implements InaNode {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitCase(
            pattern.accept(visitor),
            consequent.accept(visitor));
    }
}

/*
 * SPDX-FileCopyrightText:  © 2026 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.optimiser.constants;

import org.mina_lang.ina.Literal;

public record Constant(Literal value) implements Result {
    @Override
    public float compare(Result other) {
        return
            // constant is greater than ⊥
            other == Unassigned.VALUE ? 1.0F :
            // constant is less than ⊤
            other == NonConstant.VALUE ? -1.0F :
            // if the same constant equal otherwise incomparable
            other.equals(this) ? 0.0F : Float.NaN;
    }
}

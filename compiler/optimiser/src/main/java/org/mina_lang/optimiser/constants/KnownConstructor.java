/*
 * SPDX-FileCopyrightText:  © 2026 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.optimiser.constants;

import org.mina_lang.common.names.ConstructorName;

public record KnownConstructor(ConstructorName constructor) implements ConstructorResult {
    @Override
    public float compare(Result other) {
        return
            // known constructor is greater than ⊥
            other == Unknown.VALUE ? 1.0F :
            // known constructor is less than ⊤
            other == NonConstant.VALUE ? -1.0F :
            // known constructor is greater than constant constructor (for the same constructor)
            other instanceof ConstantConstructor constant && constant.constructor().equals(this.constructor) ? 1.0F :
            // if exactly the same known constructor equal otherwise incomparable
            other.equals(this) ? 0.0F : Float.NaN;
    }
}

/*
 * SPDX-FileCopyrightText:  © 2026 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.optimiser.constants;

import org.mina_lang.common.names.ConstructorName;

public record ConstantConstructor(ConstructorName constructor) implements ConstructorResult {
    @Override
    public float compare(Result other) {
        return
            // constant constructor is greater than ⊥
            other == Unknown.VALUE ? 1.0F :
            // constant constructor is less than ⊤
            other == NonConstant.VALUE ? -1.0F :
            // constant constructor is less than known constructor (for the same constructor)
            other instanceof KnownConstructor known && known.constructor().equals(this.constructor) ? -1.0F :
            // if exactly the same constant constructor equal otherwise incomparable
            other.equals(this) ? 0.0F : Float.NaN;
    }

    @Override
    public String toString() {
        return "ConstantConstructor[" + constructor().canonicalName() + "]";
    }
}

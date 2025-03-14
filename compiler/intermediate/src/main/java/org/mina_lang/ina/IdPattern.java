/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.names.LocalName;
import org.mina_lang.common.types.Type;

public record IdPattern(LocalName name, Type type) implements Pattern {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitIdPattern(name, type);
    }
}

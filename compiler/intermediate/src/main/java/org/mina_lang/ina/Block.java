/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.types.Type;

public record Block(Type type, ImmutableList<LocalBinding> bindings, Expression result) implements Expression {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitBlock(
            type,
            bindings.collect(binding -> binding.accept(visitor)),
            result.accept(visitor));
    }
}

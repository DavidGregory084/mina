/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.types.Type;

import java.util.List;

public record Block(Type type, List<LocalBinding> bindings, Expression result) implements Expression {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitBlock(
            type,
            bindings.stream().map(binding -> binding.accept(visitor)).toList(),
            result.accept(visitor));
    }
}

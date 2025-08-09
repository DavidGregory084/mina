/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.types.Type;

import java.util.List;

public record Apply(Type type, Value expr, List<Value> args) implements Expression {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitApply(
            type,
            expr.accept(visitor),
            args.stream().map(arg -> arg.accept(visitor)).toList());
    }
}

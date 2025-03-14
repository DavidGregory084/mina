/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.types.Type;

public record Apply(Type type, Value expr, ImmutableList<Value> args) implements Expression {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitApply(
            type,
            expr.accept(visitor),
            args.collect(arg -> arg.accept(visitor)));
    }
}

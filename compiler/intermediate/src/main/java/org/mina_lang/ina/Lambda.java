/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.types.Type;

public record Lambda(Type type, ImmutableList<Param> params, Expression body) implements Value {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitLambda(
            type,
            params.collect(param -> param.accept(visitor)),
            body.accept(visitor));
    }
}

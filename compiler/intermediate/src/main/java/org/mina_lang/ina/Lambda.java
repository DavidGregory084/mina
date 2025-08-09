/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.types.Type;

import java.util.List;

public record Lambda(Type type, List<Param> params, Expression body) implements Value {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitLambda(
            type,
            params.stream().map(param -> param.accept(visitor)).toList(),
            body.accept(visitor));
    }
}

/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.names.LocalBindingName;
import org.mina_lang.common.types.Type;

import java.util.List;

public record Join(LocalBindingName name, Type type, List<Param> params, Expression body) implements LocalBinding {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitJoin(
            name, type,
            params.stream().map(param -> param.accept(visitor)).toList(),
            body.accept(visitor));
    }
}

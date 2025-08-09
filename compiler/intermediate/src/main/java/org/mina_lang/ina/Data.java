/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.names.DataName;
import org.mina_lang.common.types.TypeVar;

import java.util.List;

public record Data(DataName name, List<TypeVar> typeParams, List<Constructor> constructors) implements Declaration {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitData(
            name, typeParams,
            constructors.stream().map(constr -> constr.accept(visitor)).toList());
    }
}

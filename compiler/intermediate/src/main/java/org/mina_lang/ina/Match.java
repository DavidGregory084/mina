/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.types.Type;

public record Match(Type type, Value scrutinee, ImmutableList<Case> cases) implements Expression {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitMatch(
            type,
            scrutinee.accept(visitor),
            cases.collect(cse -> cse.accept(visitor)));
    }
}

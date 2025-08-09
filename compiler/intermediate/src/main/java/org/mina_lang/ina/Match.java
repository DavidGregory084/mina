/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.types.Type;

import java.util.List;

public record Match(Type type, Value scrutinee, List<Case> cases) implements Expression {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitMatch(
            type,
            scrutinee.accept(visitor),
            cases.stream().map(cse -> cse.accept(visitor)).toList());
    }
}

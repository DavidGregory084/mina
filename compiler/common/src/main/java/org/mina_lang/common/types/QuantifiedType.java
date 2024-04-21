/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

import org.eclipse.collections.api.list.ImmutableList;

public record QuantifiedType(ImmutableList<TypeVar> args, Type body, Kind kind) implements PolyType {

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visitQuantifiedType(this);
    }

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitQuantifiedType(this);
    }

    @Override
    public QuantifiedType accept(TypeTransformer visitor) {
        return visitor.visitQuantifiedType(this);
    }
}

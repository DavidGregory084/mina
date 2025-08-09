/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

import java.util.List;

public record TypeApply(Type type, List<Type> typeArguments, Kind kind) implements MonoType {

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visitTypeApply(this);
    }

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitTypeApply(this);
    }

    @Override
    public TypeApply accept(TypeTransformer visitor) {
        return visitor.visitTypeApply(this);
    }
}

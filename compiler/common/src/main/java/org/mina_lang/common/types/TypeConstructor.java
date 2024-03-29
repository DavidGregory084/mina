/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

import org.mina_lang.common.names.QualifiedName;

public record TypeConstructor(QualifiedName name, Kind kind) implements MonoType {

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visitTypeConstructor(this);
    }

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitTypeConstructor(this);
    }

    @Override
    public TypeConstructor accept(TypeTransformer visitor) {
        return visitor.visitTypeConstructor(this);
    }
}

/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

public record ConstructorName(DataName enclosing, QualifiedName name) implements ValueName, TypeName {

    @Override
    public void accept(NameVisitor visitor) {
        visitor.visitConstructorName(this);
    }

    @Override
    public <A> A accept(NameFolder<A> visitor) {
        return visitor.visitConstructorName(this);
    }
}

/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

public record LetName(QualifiedName name) implements DeclarationName, ValueName {

    @Override
    public String localName() {
        return name.name();
    }

    @Override
    public String canonicalName() {
        return name.canonicalName();
    }

    @Override
    public void accept(NameVisitor visitor) {
        visitor.visitLetName(this);
    }

    @Override
    public <A> A accept(NameFolder<A> visitor) {
        return visitor.visitLetName(this);
    }
}

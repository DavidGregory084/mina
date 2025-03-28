/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

public record BuiltInName(String name) implements Named {

    @Override
    public String localName() {
        return name();
    }

    @Override
    public String canonicalName() {
        return name();
    }

    @Override
    public void accept(NameVisitor visitor) {
        visitor.visitBuiltInName(this);
    }

    @Override
    public <A> A accept(NameFolder<A> visitor) {
        return visitor.visitBuiltInName(this);
    }
}

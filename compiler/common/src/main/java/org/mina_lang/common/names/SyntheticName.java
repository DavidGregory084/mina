/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

public record SyntheticName(int index) implements LocalBindingName {
    @Override
    public String localName() {
        // User-specified identifiers cannot contain $,
        // because its Unicode Identifier_Status is Restricted
        return "$" + index;
    }

    @Override
    public String canonicalName() {
        return localName();
    }

    @Override
    public void accept(NameVisitor visitor) {
        visitor.visitSyntheticName(this);
    }

    @Override
    public <A> A accept(NameFolder<A> visitor) {
        return visitor.visitSyntheticName(this);
    }
}

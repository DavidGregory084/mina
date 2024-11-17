/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

public record ConstructorName(DataName enclosing, QualifiedName name) implements TypeName {

    @Override
    public void accept(NameVisitor visitor) {
        visitor.visitDataName(enclosing);
        visitor.visitConstructorName(this);
    }
}

/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

public record DataName(QualifiedName name) implements TypeName {

    @Override
    public void accept(NameVisitor visitor) {
        visitor.visitDataName(this);
    }
}

/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

public record ExistsVarName(String name) implements TypeVarName {

    @Override
    public void accept(NameVisitor visitor) {
        visitor.visitExistsVarName(this);
    }

    @Override
    public <A> A accept(NameFolder<A> visitor) {
        return visitor.visitExistsVarName(this);
    }
}

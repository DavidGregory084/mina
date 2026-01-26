/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

public enum Nameless implements Name {
    INSTANCE;

    @Override
    public void accept(NameVisitor visitor) {
        visitor.visitNameless(INSTANCE);
    }

    @Override
    public <A> A accept(NameFolder<A> visitor) {
        return visitor.visitNameless(this);
    }


    @Override
    public String toString() {
        return "Nameless";
    }
}

/*
 * SPDX-FileCopyrightText:  © 2022 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

public sealed interface TypeVarName extends Named permits ForAllVarName, ExistsVarName {
    public String name();

    @Override
    default public String localName() {
        return name();
    }

    @Override
    default public String canonicalName() {
        return name();
    }
}

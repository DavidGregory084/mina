/*
 * SPDX-FileCopyrightText:  © 2022 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

public sealed interface DeclarationName extends Named permits LetName, TypeName {

    public QualifiedName name();

    @Override
    default public String localName() {
        return name().name();
    }

    @Override
    default public String canonicalName() {
        return name().canonicalName();
    }
}

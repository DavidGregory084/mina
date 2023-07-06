/*
 * SPDX-FileCopyrightText:  © 2022 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

public sealed interface Named extends Name permits NamespaceName, BuiltInName, DeclarationName, FieldName, LocalName, TypeVarName {
    public String localName();
    public String canonicalName();
}

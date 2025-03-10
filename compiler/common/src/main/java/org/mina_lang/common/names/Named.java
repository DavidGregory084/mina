/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

public sealed interface Named extends Name permits ValueName, BuiltInName, DeclarationName, FieldName, NamespaceName, TypeVarName {
    public String localName();
    public String canonicalName();
}

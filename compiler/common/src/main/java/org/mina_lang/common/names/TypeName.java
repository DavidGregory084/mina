/*
 * SPDX-FileCopyrightText:  © 2022 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

public sealed interface TypeName extends DeclarationName permits DataName, ConstructorName {

}

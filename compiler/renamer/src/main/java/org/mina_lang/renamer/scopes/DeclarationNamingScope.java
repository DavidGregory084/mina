/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.renamer.scopes;

import org.mina_lang.common.names.DeclarationName;

public interface DeclarationNamingScope extends NamingScope {
    DeclarationName declarationName();
}

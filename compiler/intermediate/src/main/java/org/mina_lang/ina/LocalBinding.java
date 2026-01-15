/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.names.LocalBindingName;

public sealed interface LocalBinding extends InaNode permits Join, LetAssign {
    LocalBindingName name();
    Expression body();
}

/*
 * SPDX-FileCopyrightText:  © 2026 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.optimiser.constants;

import org.mina_lang.common.names.ConstructorName;

public sealed interface ConstructorResult extends Result permits KnownConstructor, ConstantConstructor {
    ConstructorName constructor();
}

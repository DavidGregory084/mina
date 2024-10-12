/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.testing;

import org.eclipse.collections.api.stack.MutableStack;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Environment;

public record GenEnvironment(
    MutableStack<GenScope> scopes
) implements Environment<Attributes, GenScope> {}

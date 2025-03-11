/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.names.NamespaceName;

public record Namespace(NamespaceName name, ImmutableList<Declaration> declarations) implements InaNode {
}

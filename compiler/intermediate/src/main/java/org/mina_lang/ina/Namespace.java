/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.ina;

import org.mina_lang.common.names.NamespaceName;

import java.util.List;

public record Namespace(NamespaceName name, List<Declaration> declarations) implements InaNode {
    @Override
    public <A> A accept(InaNodeFolder<A> visitor) {
        return visitor.visitNamespace(
            name,
            declarations.stream().map(decl -> decl.accept(visitor)).toList());
    }
}

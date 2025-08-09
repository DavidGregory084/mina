/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import com.opencastsoftware.yvette.Range;

import java.util.List;

public record ImportSymbolsNode(Range range, NamespaceIdNode namespace, List<ImporteeNode> symbols) implements ImportNode {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        namespace.accept(visitor);
        symbols.forEach(sym -> sym.accept(visitor));
        visitor.visitImport(this);
    }
}

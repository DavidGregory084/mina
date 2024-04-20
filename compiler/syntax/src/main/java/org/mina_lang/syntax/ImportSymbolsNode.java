/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import com.opencastsoftware.yvette.Range;
import org.eclipse.collections.api.list.ImmutableList;

public record ImportSymbolsNode(Range range, NamespaceIdNode namespace, ImmutableList<ImporteeNode> symbols) implements ImportNode {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        namespace.accept(visitor);
        symbols.forEach(sym -> sym.accept(visitor));
        visitor.visitImport(this);
    }
}

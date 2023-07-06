/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import com.opencastsoftware.yvette.Range;
import org.eclipse.collections.api.list.ImmutableList;

public record ImportNode(Range range, NamespaceIdNode namespace, ImmutableList<ImportSymbolNode> symbols)
        implements SyntaxNode {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        namespace.accept(visitor);
        symbols.forEach(sym -> sym.accept(visitor));
        visitor.visitImport(this);
    }
}

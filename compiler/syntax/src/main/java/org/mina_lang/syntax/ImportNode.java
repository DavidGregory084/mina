package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;

import com.opencastsoftware.yvette.Range;

public record ImportNode(Range range, NamespaceIdNode namespace, ImmutableList<ImportSymbolNode> symbols)
        implements SyntaxNode {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        namespace.accept(visitor);
        symbols.forEach(sym -> sym.accept(visitor));
        visitor.visitImport(this);
    }
}

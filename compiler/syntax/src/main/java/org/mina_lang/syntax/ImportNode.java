package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Range;

public record ImportNode(Range range, NamespaceIdNode namespace, ImmutableList<ImportSymbolNode> symbols) implements SyntaxNode {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        namespace.accept(visitor);
        visitor.visitImport(this);
    }
}

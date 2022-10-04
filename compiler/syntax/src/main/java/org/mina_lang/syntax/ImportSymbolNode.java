package org.mina_lang.syntax;

import org.mina_lang.common.Range;

public record ImportSymbolNode(Range range, String symbol) implements SyntaxNode {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitImportSymbol(this);
    }
}

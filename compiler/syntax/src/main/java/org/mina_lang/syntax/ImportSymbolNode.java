package org.mina_lang.syntax;

import com.opencastsoftware.yvette.Range;

public record ImportSymbolNode(Range range, String symbol) implements SyntaxNode {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitImportSymbol(this);
    }
}

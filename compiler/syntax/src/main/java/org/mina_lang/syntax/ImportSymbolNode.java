package org.mina_lang.syntax;

import com.opencastsoftware.yvette.Range;

import java.util.Optional;

public record ImportSymbolNode(Range range, String symbol, Optional<String> alias) implements SyntaxNode {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitImportSymbol(this);
    }
}

package org.mina_lang.langserver;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.lsp4j.Range;
import org.mina_lang.syntax.SyntaxNode;

public class MinaSyntaxNodeCollector {
    MutableList<Pair<Range, SyntaxNode>> syntaxNodes = Lists.mutable.empty();

    public void add(Pair<Range, SyntaxNode> node) {
        syntaxNodes.add(node);
    }

    public ImmutableList<Pair<Range, SyntaxNode>> getSyntaxNodes() {
        return syntaxNodes.toImmutable();
    }
}

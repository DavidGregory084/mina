package org.mina_lang.parser;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.lsp4j.Range;
import org.mina_lang.syntax.SyntaxNode;

public class SyntaxNodeCollector<A> {
    MutableList<Pair<Range, SyntaxNode<A>>> syntaxNodes = Lists.mutable.empty();

    public void add(Pair<Range, SyntaxNode<A>> node) {
        syntaxNodes.add(node);
    }

    public ImmutableList<Pair<Range, SyntaxNode<A>>> getSyntaxNodes() {
        return syntaxNodes.toImmutable();
    }
}

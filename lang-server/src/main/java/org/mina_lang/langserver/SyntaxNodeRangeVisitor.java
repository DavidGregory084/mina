package org.mina_lang.langserver;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.mina_lang.syntax.MetaNode;
import org.mina_lang.syntax.SyntaxNode;
import org.mina_lang.syntax.SyntaxNodeVisitor;

public class SyntaxNodeRangeVisitor implements SyntaxNodeVisitor {
    MutableSortedMap<Range, MetaNode<?>> rangeNodes = SortedMaps.mutable.of(new RangeComparator());

    public ImmutableSortedMap<Range, MetaNode<?>> getRangeNodes() {
        return rangeNodes.toImmutable();
    }

    @Override
    public void visit(SyntaxNode node) {
        var start = node.range().start();

        var end = node.range().end();

        var range = new Range(
                new Position(start.line(), start.character()),
                new Position(end.line(), end.character()));

        if (node instanceof MetaNode<?> metaNode) {
            rangeNodes.put(range, metaNode);
        }
    }
}

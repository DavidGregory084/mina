package org.mina_lang.langserver;

import java.util.Comparator;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public class RangeComparator implements Comparator<Range> {

    private Comparator<Position> posComparator = new PositionComparator();

    @Override
    public int compare(Range left, Range right) {
        var startComparison = posComparator
                .reversed()
                .compare(left.getStart(), right.getStart());

        if (startComparison != 0) {
            return startComparison;
        } else {
            return posComparator.compare(left.getEnd(), right.getEnd());
        }
    }

}

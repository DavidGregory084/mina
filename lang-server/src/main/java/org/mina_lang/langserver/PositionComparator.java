package org.mina_lang.langserver;

import java.util.Comparator;

import org.eclipse.lsp4j.Position;

public class PositionComparator implements Comparator<Position> {
    @Override
    public int compare(Position left, Position right) {
        var lineComparison = Integer.compare(left.getLine(), right.getLine());

        if (lineComparison != 0) {
            return lineComparison;
        } else {
            return Integer.compare(left.getCharacter(), right.getCharacter());
        }
    }
}

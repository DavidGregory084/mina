package org.mina_lang.common;

public record Position(int line, int character) implements Comparable<Position> {
    public static Position NONE = new Position(-1, -1);

    @Override
    public int compareTo(Position that) {
        var lineComparison = Integer.compare(this.line(), that.line());

        if (lineComparison != 0) {
            return lineComparison;
        } else {
            return Integer.compare(this.character(), that.character());
        }
    }
}

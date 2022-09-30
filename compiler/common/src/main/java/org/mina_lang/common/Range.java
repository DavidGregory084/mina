package org.mina_lang.common;

public record Range(Position start, Position end) {
    public static Range EMPTY = new Range(Position.NONE, Position.NONE);

    public Range(int startLine, int startChar, int endLine, int endChar) {
        this(new Position(startLine, startChar), new Position(endLine, endChar));
    }

    public Range(int startLine, int startChar, int length) {
        this(new Position(startLine, startChar), new Position(startLine, startChar + length));
    }
}

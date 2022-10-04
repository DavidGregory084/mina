package org.mina_lang.common;

public record Position(int line, int character) {
    public static Position NONE = new Position(-1, -1);

}

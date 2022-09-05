package org.mina_lang.common;

public record TypeVarName(String name) implements Named {

    @Override
    public String localName() {
        return name();
    }

    @Override
    public String canonicalName() {
        return name();
    }
}

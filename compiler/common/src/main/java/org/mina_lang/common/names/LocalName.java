package org.mina_lang.common.names;

public record LocalName(String name, int index) implements Named {

    @Override
    public String localName() {
        return name();
    }

    @Override
    public String canonicalName() {
        return name();
    }
}

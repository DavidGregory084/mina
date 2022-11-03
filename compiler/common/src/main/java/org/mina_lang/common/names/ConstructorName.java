package org.mina_lang.common.names;

public record ConstructorName(DataName enclosing, QualifiedName name) implements Named {

    @Override
    public String localName() {
        return name.name();
    }

    @Override
    public String canonicalName() {
        return name.canonicalName();
    }
}

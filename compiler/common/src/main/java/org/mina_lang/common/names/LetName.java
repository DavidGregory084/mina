package org.mina_lang.common.names;

public record LetName(QualifiedName name) implements DeclarationName {

    @Override
    public String localName() {
        return name.name();
    }

    @Override
    public String canonicalName() {
        return name.canonicalName();
    }
}

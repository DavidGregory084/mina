package org.mina_lang.common;

public sealed interface Named extends Name permits NamespaceName, LetName, DataName, ConstructorName, LocalName, TypeName, TypeVarName {
    public String localName();
    public String canonicalName();
}

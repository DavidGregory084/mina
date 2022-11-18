package org.mina_lang.common.names;

public sealed interface Named extends Name permits NamespaceName, BuiltInName, LetName, DataName, ConstructorName, FieldName, LocalName, TypeName, TypeVarName {
    public String localName();
    public String canonicalName();
}

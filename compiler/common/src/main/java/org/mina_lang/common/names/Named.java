package org.mina_lang.common.names;

public sealed interface Named extends Name permits NamespaceName, BuiltInName, DeclarationName, FieldName, LocalName, TypeVarName {
    public String localName();
    public String canonicalName();
}

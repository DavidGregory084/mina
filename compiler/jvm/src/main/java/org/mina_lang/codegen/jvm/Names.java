package org.mina_lang.codegen.jvm;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.names.*;
import org.mina_lang.syntax.*;

public class Names {
    public static String getDescriptor(NamespaceName name) {
        return "L" + getInternalName(name) + ";";
    }

    public static String getDescriptor(QualifiedName name) {
        return "L" + getInternalName(name) + ";";
    }

    public static String getDescriptor(LetName name) {
        return "L" + getInternalName(name.name().ns()) + "." + name.name().name() + ";";
    }

    public static String getDescriptor(Named name) {
        return "L" + getInternalName(name) + ";";
    }

    public static String getDescriptor(NamespaceNode<Attributes> node) {
        return getDescriptor(getName(node));
    }

    public static String getDescriptor(MetaNode<Attributes> node) {
        return getDescriptor(getName(node));
    }

    public static String getInternalName(NamespaceName name) {
        return name.canonicalName().replaceAll("\\.", "/") + "/$namespace";
    }

    public static String getInternalName(QualifiedName name) {
        return name.canonicalName().replaceAll("\\.", "/");
    }

    public static String getInternalName(Named name) {
        return name.canonicalName().replaceAll("\\.", "/");
    }

    public static String getInternalName(NamespaceNode<Attributes> node) {
        return getInternalName(getName(node));
    }

    public static String getInternalName(MetaNode<Attributes> node) {
        return getInternalName(getName(node));
    }

    public static Named getName(MetaNode<Attributes> node) {
        return (Named) node.meta().meta().name();
    }

    public static NamespaceName getName(NamespaceNode<Attributes> node) {
        return (NamespaceName) node.meta().meta().name();
    }

    public static DataName getName(DataNode<Attributes> node) {
        return (DataName) node.meta().meta().name();
    }

    public static ConstructorName getName(ConstructorNode<Attributes> node) {
        return (ConstructorName) node.meta().meta().name();
    }
}

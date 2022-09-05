package org.mina_lang.common;

public record QualifiedName(NamespaceName ns, String name) {
    public String canonicalName() {
        return ns.canonicalName() + "." + name();
    }
}

package org.mina_lang.common.names;

public record QualifiedName(NamespaceName ns, String name) {
    public String canonicalName() {
        return ns.canonicalName() + "." + name();
    }
}

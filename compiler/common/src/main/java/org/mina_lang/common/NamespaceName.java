package org.mina_lang.common;

import org.eclipse.collections.api.list.ImmutableList;

public record NamespaceName(ImmutableList<String> pkg, String name) implements Named {

    @Override
    public String localName() {
        return name();
    }

    @Override
    public String canonicalName() {
        var segments = pkg().toList();
        segments.add(name());
        return segments.makeString("/");
    }
}

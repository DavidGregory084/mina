/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

import java.util.List;
import java.util.stream.Collectors;

public record NamespaceName(List<String> pkg, String name) implements Named {

    @Override
    public String localName() {
        return name();
    }

    @Override
    public String canonicalName() {
        var segments = new java.util.ArrayList<>(pkg());
        segments.add(name());
        return String.join("/", segments);
    }

    @Override
    public void accept(NameVisitor visitor) {
        visitor.visitNamespaceName(this);
    }

    @Override
    public <A> A accept(NameFolder<A> visitor) {
        return visitor.visitNamespaceName(this);
    }
}

/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

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

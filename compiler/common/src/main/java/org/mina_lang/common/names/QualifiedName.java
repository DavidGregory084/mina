/*
 * SPDX-FileCopyrightText:  © 2022 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.names;

public record QualifiedName(NamespaceName ns, String name) {
    public String canonicalName() {
        return ns.canonicalName() + "." + name();
    }
}

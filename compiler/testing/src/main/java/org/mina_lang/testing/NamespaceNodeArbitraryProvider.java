/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.testing;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;
import org.mina_lang.common.Attributes;
import org.mina_lang.syntax.NamespaceNode;

import java.util.Collections;
import java.util.Set;

public class NamespaceNodeArbitraryProvider implements ArbitraryProvider {
    @Override
    public boolean canProvideFor(TypeUsage typeUsage) {
        return typeUsage.canBeAssignedTo(TypeUsage.of(NamespaceNode.class, TypeUsage.of(Attributes.class)));
    }

    @Override
    public Set<Arbitrary<?>> provideFor(TypeUsage typeUsage, SubtypeProvider subtypeProvider) {
        return Collections.singleton(SyntaxArbitraries.namespaceNode);
    }
}

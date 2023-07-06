/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import com.opencastsoftware.yvette.Range;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.common.names.QualifiedName;

import java.util.Optional;

public record QualifiedIdNode (Range range, Optional<NamespaceIdNode> ns, String name) implements SyntaxNode {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        ns.ifPresent(id -> id.accept(visitor));
        visitor.visitQualifiedId(this);
    }

    public String canonicalName() {
        return ns().map(ns -> ns.getName().canonicalName() + ".").orElse("") + name();
    }

    public QualifiedName getName(NamespaceName currentNamespace) {
        return new QualifiedName(
            ns.map(NamespaceIdNode::getName).orElse(currentNamespace),
            name);
    }
}

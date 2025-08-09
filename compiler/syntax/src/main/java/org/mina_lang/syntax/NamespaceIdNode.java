/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import com.opencastsoftware.yvette.Range;
import org.mina_lang.common.names.NamespaceName;

import java.util.List;

public record NamespaceIdNode (Range range, List<String> pkg, String ns) implements SyntaxNode {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitNamespaceId(this);
    }

    public NamespaceName getName() {
        return new NamespaceName(pkg, ns);
    }
}

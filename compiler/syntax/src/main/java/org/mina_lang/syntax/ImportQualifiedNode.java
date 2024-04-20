/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import com.opencastsoftware.yvette.Range;

import java.util.Optional;

public record ImportQualifiedNode(Range range, NamespaceIdNode namespace, Optional<String> alias) implements ImportNode {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        namespace.accept(visitor);
        visitor.visitImport(this);
    }
}

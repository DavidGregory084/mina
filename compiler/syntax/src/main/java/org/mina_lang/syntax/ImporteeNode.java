/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import com.opencastsoftware.yvette.Range;

import java.util.Optional;

public record ImporteeNode(Range range, String symbol, Optional<String> alias) implements SyntaxNode {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitImportee(this);
    }
}

/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;


sealed public interface ImportNode extends SyntaxNode permits ImportQualifiedNode, ImportSymbolsNode {
    NamespaceIdNode namespace();
}

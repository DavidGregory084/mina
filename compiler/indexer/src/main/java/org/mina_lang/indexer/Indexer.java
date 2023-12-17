/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.indexer;

import com.sourcegraph.Scip.Document;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.diagnostics.ScopedDiagnosticCollector;
import org.mina_lang.syntax.NamespaceNode;

public class Indexer {
    private static final String LANGUAGE_ID = "mina";

    private final ScopedDiagnosticCollector diagnostics;

    public Indexer(ScopedDiagnosticCollector diagnostics) {
        this.diagnostics = diagnostics;
    }

    public Document index(NamespaceNode<Attributes> namespace) {
        var builder = Document.newBuilder();
        builder.setLanguage(LANGUAGE_ID);
        indexNamespace(builder, namespace);
        return builder.build();
    }

    public void indexNamespace(Document.Builder builder, NamespaceNode<Attributes> namespace) {
    }
}

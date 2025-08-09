/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.documents;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentItem;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MinaTextDocuments {
    private final ConcurrentHashMap<String, TextDocumentItem> documents = new ConcurrentHashMap<>();

    public TextDocumentItem getDocument(String uri) {
        return documents.get(uri);
    }

    public void addDocument(DidOpenTextDocumentParams params) {
        var document = params.getTextDocument();
        documents.put(document.getUri(), document);
    }

    public void removeDocument(DidCloseTextDocumentParams params) {
        var document = params.getTextDocument();
        documents.remove(document.getUri());
    }

    public TextDocumentItem updateDocument(DidChangeTextDocumentParams params) {
        var newDocument = params.getTextDocument();
        return documents.computeIfPresent(newDocument.getUri(), (uri, existingDocument) -> {
            return TextDocument.applyChanges(existingDocument, params);
        });
    }

    public Set<TextDocumentItem> getAllDocuments() {
        return new HashSet<>(documents.values());
    }
}

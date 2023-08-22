/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.documents;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.mina_lang.langserver.MinaLanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


public class MinaTextDocumentService implements TextDocumentService {
    private static Logger logger = LoggerFactory.getLogger(MinaTextDocumentService.class);

    private MinaLanguageServer server;
    private MinaTextDocuments documents = new MinaTextDocuments();

    public MinaTextDocumentService(MinaLanguageServer server) {
        this.server = server;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        server.ifShouldNotify(() -> {
            documents.addDocument(params);
        });
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        server.ifShouldNotify(() -> {
            documents.updateDocument(params);
        });
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        server.ifShouldNotify(() -> {
            documents.removeDocument(params);
        });
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
    }

    public Set<TextDocumentItem> getAllDocuments() {
        return documents.getAllDocuments();
    }
}

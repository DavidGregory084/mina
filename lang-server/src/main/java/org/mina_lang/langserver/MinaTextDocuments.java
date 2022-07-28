package org.mina_lang.langserver;

import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentItem;

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
}

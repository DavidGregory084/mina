package org.mina_lang.langserver;

import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentItem;

public class MinaTextDocuments {
    private final ConcurrentHashMap<String, TextDocumentItem> documents = new ConcurrentHashMap<>();

    public TextDocumentItem get(String uri) {
        return documents.get(uri);
    }

    public void addDocument(DidOpenTextDocumentParams params) {
        var document = params.getTextDocument();
        documents.put(document.getUri(), document);
    }

    public void updateDocument(DidChangeTextDocumentParams params) {
        var newDocument = params.getTextDocument();
        var changes = params.getContentChanges();
        if (!changes.isEmpty()) {
            var existingDocument = get(newDocument.getUri());
            if (existingDocument != null) {
                var latestText = changes.get(changes.size() - 1).getText();
                existingDocument.setVersion(newDocument.getVersion());
                existingDocument.setText(latestText);
            }
        }
    }

    public void removeDocument(DidCloseTextDocumentParams params) {
        var document = params.getTextDocument();
        documents.remove(document.getUri());
    }
}

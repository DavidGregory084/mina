package org.mina_lang.langserver;

import org.eclipse.lsp4j.DidChangeNotebookDocumentParams;
import org.eclipse.lsp4j.DidCloseNotebookDocumentParams;
import org.eclipse.lsp4j.DidOpenNotebookDocumentParams;
import org.eclipse.lsp4j.DidSaveNotebookDocumentParams;
import org.eclipse.lsp4j.services.NotebookDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinaNotebookDocumentService implements NotebookDocumentService {
    private Logger logger = LoggerFactory.getLogger(MinaNotebookDocumentService.class);

    private MinaLanguageServer server;

    public MinaNotebookDocumentService(MinaLanguageServer server) {
        this.server = server;
    }

    @Override
    public void didOpen(DidOpenNotebookDocumentParams params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void didChange(DidChangeNotebookDocumentParams params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void didSave(DidSaveNotebookDocumentParams params) {
        // TODO Auto-generated method stub

    }

    @Override
    public void didClose(DidCloseNotebookDocumentParams params) {
        // TODO Auto-generated method stub

    }
}

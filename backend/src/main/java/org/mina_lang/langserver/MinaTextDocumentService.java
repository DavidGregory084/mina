package org.mina_lang.langserver;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.mina_lang.langserver.semantic.tokens.SemanticTokensParser;
import org.mina_lang.parser.CompilationUnitParser;

public class MinaTextDocumentService implements TextDocumentService {

    private MinaLanguageServer server;
    private MinaTextDocuments documents = new MinaTextDocuments();

    public MinaTextDocumentService(MinaLanguageServer server) {
        this.server = server;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        server.ifShouldNotify(() -> {
            var document = params.getTextDocument();
            documents.addDocument(params);
            var diagnosticCollector = new MinaDiagnosticCollector();
            CompilationUnitParser.parse(document, diagnosticCollector);
            server.getClient().publishDiagnostics(
                    new PublishDiagnosticsParams(document.getUri(), diagnosticCollector.getDiagnostics()));
        });
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        server.ifShouldNotify(() -> {
            documents.updateDocument(params);
            var newDocument = documents.get(params.getTextDocument().getUri());
            var diagnosticCollector = new MinaDiagnosticCollector();
            CompilationUnitParser.parse(newDocument, diagnosticCollector);
            server.getClient().publishDiagnostics(
                    new PublishDiagnosticsParams(newDocument.getUri(), diagnosticCollector.getDiagnostics()));
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

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
        return server.ifInitialized(() -> {
            return CompletableFutures.computeAsync(server.getExecutor(), cancelToken -> {
                cancelToken.checkCanceled();
                var document = documents.get(params.getTextDocument().getUri());
                var tokens = SemanticTokensParser.parseTokens(document);
                return new SemanticTokens(tokens);
            });
        });
    }
}

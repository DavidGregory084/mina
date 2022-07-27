package org.mina_lang.langserver;

import org.antlr.v4.runtime.CharStreams;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.mina_lang.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class MinaTextDocumentService implements TextDocumentService {
    private Logger logger = LoggerFactory.getLogger(MinaTextDocumentService.class);

    private MinaLanguageServer server;
    private MinaTextDocuments documents = new MinaTextDocuments();
    private MinaSyntaxTrees syntaxTrees = new MinaSyntaxTrees();

    public MinaTextDocumentService(MinaLanguageServer server) {
        this.server = server;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        server.ifShouldNotify(() -> {
            var document = params.getTextDocument();
            documents.addDocument(params);
            var parsingFuture = CompletableFuture.supplyAsync(() -> {
                var diagnosticCollector = new MinaDiagnosticCollector();
                try {
                    var charStream = CharStreams.fromString(document.getText(), document.getUri());
                    return new Parser(diagnosticCollector).parse(charStream);
                } finally {
                    server.getClient().publishDiagnostics(
                            new PublishDiagnosticsParams(
                                    document.getUri(),
                                    diagnosticCollector.getLSPDiagnostics(),
                                    document.getVersion()));
                }
            }, server.getExecutor());
            syntaxTrees.addSyntaxTree(params, parsingFuture);
        });
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        server.ifShouldNotify(() -> {
            documents.updateDocument(params);
            var documentUri = params.getTextDocument().getUri();
            var newDocument = documents.get(documentUri);
            var parsingFuture = CompletableFuture.supplyAsync(() -> {
                var diagnosticCollector = new MinaDiagnosticCollector();
                try {
                    var charStream = CharStreams.fromString(newDocument.getText(), documentUri);
                    return new Parser(diagnosticCollector).parse(charStream);
                } finally {
                    server.getClient().publishDiagnostics(
                            new PublishDiagnosticsParams(
                                    newDocument.getUri(),
                                    diagnosticCollector.getLSPDiagnostics(),
                                    newDocument.getVersion()));
                }
            }, server.getExecutor());
            syntaxTrees.updateSyntaxTree(params, parsingFuture);
        });
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        server.ifShouldNotify(() -> {
            documents.removeDocument(params);
            syntaxTrees.removeSyntaxTree(params);
        });
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
    }
}

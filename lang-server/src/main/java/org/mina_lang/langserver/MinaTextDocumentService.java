package org.mina_lang.langserver;

import org.antlr.v4.runtime.CharStreams;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.mina_lang.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class MinaTextDocumentService implements TextDocumentService {
    private Logger logger = LoggerFactory.getLogger(MinaTextDocumentService.class);

    private MinaLanguageServer server;
    private MinaTextDocuments documents = new MinaTextDocuments();
    private MinaSyntaxTrees syntaxTrees = new MinaSyntaxTrees();

    public MinaTextDocumentService(MinaLanguageServer server) {
        this.server = server;
    }

    private <A> A withDiagnostics(TextDocumentItem document, Function<MinaDiagnosticCollector, A> action) {
        var diagnostics = new MinaDiagnosticCollector();
        try {
            return action.apply(diagnostics);
        } finally {
            server.getClient().publishDiagnostics(
                    new PublishDiagnosticsParams(
                            document.getUri(),
                            diagnostics.getLSPDiagnostics(),
                            document.getVersion()));
        }
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        server.ifShouldNotify(() -> {
            var document = params.getTextDocument();
            var documentUri = document.getUri();
            documents.addDocument(params);
            var parsingFuture = CompletableFuture.supplyAsync(() -> {
                return withDiagnostics(document, diagnostics -> {
                    var charStream = CharStreams.fromString(document.getText(), documentUri);
                    return new Parser(diagnostics).parse(charStream);
                });
            }, server.getExecutor());
            syntaxTrees.addSyntaxTree(params, parsingFuture);
        });
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        server.ifShouldNotify(() -> {
            var documentUri = params.getTextDocument().getUri();
            var updatedDocument = documents.updateDocument(params);
            var parsingFuture = CompletableFuture.supplyAsync(() -> {
                return withDiagnostics(updatedDocument, diagnostics -> {
                    var charStream = CharStreams.fromString(updatedDocument.getText(), documentUri);
                    return new Parser(diagnostics).parse(charStream);
                });
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

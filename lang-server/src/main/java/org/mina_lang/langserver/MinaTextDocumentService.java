package org.mina_lang.langserver;

import java.util.concurrent.CompletableFuture;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.util.Ranges;
import org.mina_lang.langserver.semantic.tokens.MinaSemanticTokensParser;
import org.mina_lang.parser.CompilationUnitParser;
import org.mina_lang.parser.SyntaxNodeCollector;
import org.mina_lang.syntax.SyntaxNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinaTextDocumentService implements TextDocumentService {
    private Logger logger = LoggerFactory.getLogger(MinaTextDocumentService.class);

    private MinaLanguageServer server;
    private MinaTextDocuments documents = new MinaTextDocuments();
    private MinaSyntaxTrees syntaxTrees = new MinaSyntaxTrees();
    private MinaHoverRanges hoverRanges = new MinaHoverRanges();

    public MinaTextDocumentService(MinaLanguageServer server) {
        this.server = server;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        server.ifShouldNotify(() -> {
            var document = params.getTextDocument();
            documents.addDocument(params);
            var hoversFuture = new CompletableFuture<ImmutableList<Pair<Range, SyntaxNode>>>();
            hoverRanges.addHoverRanges(params, hoversFuture);
            var parsingFuture = CompletableFuture.supplyAsync(() -> {
                var diagnosticCollector = new MinaDiagnosticCollector();
                var syntaxNodeCollector = new SyntaxNodeCollector();
                try {
                    var syntaxTree = CompilationUnitParser.parse(document, syntaxNodeCollector, diagnosticCollector);
                    hoversFuture.complete(syntaxNodeCollector.getSyntaxNodes());
                    return syntaxTree;
                } catch (Throwable t) {
                    hoversFuture.completeExceptionally(t);
                    throw t;
                } finally {
                    server.getClient().publishDiagnostics(
                            new PublishDiagnosticsParams(document.getUri(), diagnosticCollector.getDiagnostics()));
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
            var hoversFuture = new CompletableFuture<ImmutableList<Pair<Range, SyntaxNode>>>();
            hoverRanges.updateHoverRanges(params, hoversFuture);
            var parsingFuture = CompletableFuture.supplyAsync(() -> {
                var diagnosticCollector = new MinaDiagnosticCollector();
                var syntaxNodeCollector = new SyntaxNodeCollector();
                try {
                    var syntaxTree = CompilationUnitParser.parse(newDocument, syntaxNodeCollector, diagnosticCollector);
                    hoversFuture.complete(syntaxNodeCollector.getSyntaxNodes());
                    return syntaxTree;
                } catch (Throwable t) {
                    hoversFuture.completeExceptionally(t);
                    throw t;
                } finally {
                    server.getClient().publishDiagnostics(
                            new PublishDiagnosticsParams(newDocument.getUri(), diagnosticCollector.getDiagnostics()));
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
            hoverRanges.removeHoverRanges(params);
        });
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
        return server.ifInitialized(cancelToken -> {
            cancelToken.checkCanceled();
            var document = documents.get(params.getTextDocument().getUri());
            var tokens = MinaSemanticTokensParser.parseTokens(document);
            cancelToken.checkCanceled();
            return new SemanticTokens(tokens);
        });
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        return server.ifInitializedAsync(cancelToken -> {
            var documentUri = params.getTextDocument().getUri();
            var docHoverRanges = hoverRanges.get(documentUri);
            if (docHoverRanges != null) {
                return docHoverRanges.thenApplyAsync(ranges -> {
                    var hoverPosition = params.getPosition();
                    var minRange = ranges
                            .select(pair -> Ranges.containsPosition(pair.getOne(), hoverPosition))
                            .minByOptional(pair -> {
                                var range = pair.getOne();
                                var startLine = range.getStart().getLine() * 1000;
                                var startChar = range.getStart().getCharacter();
                                var endLine = range.getEnd().getLine() * 1000;
                                var endChar = range.getEnd().getCharacter();
                                return (endLine + endChar) - (startLine + startChar);
                            });

                    return minRange
                            .map(pair -> {
                                var markup = new MarkupContent(MarkupKind.PLAINTEXT, pair.getTwo().toString());
                                return new Hover(markup, pair.getOne());
                            })
                            .orElse(null);
                });
            } else {
                return null;
            }
        });
    }
}

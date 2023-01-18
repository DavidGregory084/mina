package org.mina_lang.langserver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.antlr.v4.runtime.CharStreams;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.names.LocalName;
import org.mina_lang.common.names.Named;
import org.mina_lang.common.types.KindPrinter;
import org.mina_lang.common.types.SortPrinter;
import org.mina_lang.common.types.TypePrinter;
import org.mina_lang.main.Main;
import org.mina_lang.syntax.MetaNode;
import org.mina_lang.syntax.NamespaceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinaTextDocumentService implements TextDocumentService {
    private Logger logger = LoggerFactory.getLogger(MinaTextDocumentService.class);

    private MinaLanguageServer server;
    private MinaTextDocuments documents = new MinaTextDocuments();
    private MinaSyntaxTrees syntaxTrees = new MinaSyntaxTrees();
    private MinaHoverRanges hoverRanges = new MinaHoverRanges();
    private SortPrinter sortPrinter = new SortPrinter(new KindPrinter(), new TypePrinter());
    private Path storagePath = Paths.get(System.getProperty("STORAGE_FOLDER", "./classes"));

    public MinaTextDocumentService(MinaLanguageServer server) {
        this.server = server;
    }

    private <A> CompletableFuture<A> withDiagnostics(TextDocumentItem document,
            Function<MinaDiagnosticCollector, CompletableFuture<A>> action) {
        var diagnostics = new MinaDiagnosticCollector();
        return action.apply(diagnostics).thenApply(result -> {
            server.getClient().publishDiagnostics(
                    new PublishDiagnosticsParams(
                            document.getUri(),
                            diagnostics.getLSPDiagnostics(document.getUri()),
                            document.getVersion()));
            return result;
        });
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        server.ifShouldNotify(() -> {
            var document = params.getTextDocument();
            var documentUri = document.getUri();
            documents.addDocument(params);

            var hoversFuture = new CompletableFuture<ImmutableSortedMap<Range, MetaNode<?>>>();
            hoverRanges.addHoverRanges(params, hoversFuture);

            CompletableFuture<NamespaceNode<?>> parsingFuture = withDiagnostics(document, diagnostics -> {
                var charStream = CharStreams.fromString(document.getText(), documentUri);
                var compilerMain = new Main(diagnostics);
                return compilerMain.compileNamespace(storagePath, charStream);
            });

            parsingFuture.whenComplete((namespaceNode, exception) -> {
                if (exception != null) {
                    hoversFuture.completeExceptionally(exception);
                } else if (namespaceNode != null) {
                    var rangeVisitor = new SyntaxNodeRangeVisitor();
                    namespaceNode.accept(rangeVisitor);
                    hoversFuture.complete(rangeVisitor.getRangeNodes());
                }
            });

            syntaxTrees.addSyntaxTree(params, parsingFuture);
        });
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        server.ifShouldNotify(() -> {
            var documentUri = params.getTextDocument().getUri();
            var updatedDocument = documents.updateDocument(params);

            var hoversFuture = new CompletableFuture<ImmutableSortedMap<Range, MetaNode<?>>>();
            hoverRanges.updateHoverRanges(params, hoversFuture);

            CompletableFuture<NamespaceNode<?>> parsingFuture = withDiagnostics(updatedDocument, diagnostics -> {
                var charStream = CharStreams.fromString(updatedDocument.getText(), documentUri);
                var compilerMain = new Main(diagnostics);
                return compilerMain.compileNamespace(storagePath, charStream);
            });

            parsingFuture.whenComplete((namespaceNode, exception) -> {
                if (exception != null) {
                    hoversFuture.completeExceptionally(exception);
                } else if (namespaceNode != null) {
                    var rangeVisitor = new SyntaxNodeRangeVisitor();
                    namespaceNode.accept(rangeVisitor);
                    hoversFuture.complete(rangeVisitor.getRangeNodes());
                }
            });

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

    private Hover formatNode(Range range, MetaNode<?> node) {
        if (node.meta().meta() instanceof Attributes attrs) {
            String nameString;

            if (attrs.name() instanceof LocalName name) {
                nameString = name.canonicalName() + "@" + name.index() + ": ";
            } else if (attrs.name() instanceof Named name) {
                nameString = name.canonicalName() + ": ";
            } else {
                nameString = "";
            }

            var sortString = attrs.sort()
                    .accept(sortPrinter)
                    .render(80);

            var markup = new MarkupContent(
                    MarkupKind.MARKDOWN,
                    "```mina\n" + nameString + sortString + "\n```");

            return new Hover(markup, range);
        } else {
            return null;
        }
    }

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        return server.ifInitializedAsync(cancelToken -> {
            cancelToken.checkCanceled();

            return hoverRanges
                    .get(params.getTextDocument().getUri(), params.getPosition())
                    .handle((pair, ex) -> {
                        if (pair != null) {
                            return formatNode(pair.getOne(), pair.getTwo());
                        } else {
                            return null;
                        }
                    });
        });
    }
}

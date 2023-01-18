package org.mina_lang.langserver;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.antlr.v4.runtime.CharStreams;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.mina_lang.codegen.jvm.CodeGenerator;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.names.LocalName;
import org.mina_lang.common.names.Named;
import org.mina_lang.common.types.KindPrinter;
import org.mina_lang.common.types.SortPrinter;
import org.mina_lang.common.types.TypePrinter;
import org.mina_lang.parser.Parser;
import org.mina_lang.renamer.NameEnvironment;
import org.mina_lang.renamer.Renamer;
import org.mina_lang.syntax.MetaNode;
import org.mina_lang.syntax.NamespaceNode;
import org.mina_lang.typechecker.TypeEnvironment;
import org.mina_lang.typechecker.Typechecker;
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

    private <A> A withDiagnostics(TextDocumentItem document, Function<MinaDiagnosticCollector, A> action) {
        var diagnostics = new MinaDiagnosticCollector();
        try {
            return action.apply(diagnostics);
        } finally {
            server.getClient().publishDiagnostics(
                    new PublishDiagnosticsParams(
                            document.getUri(),
                            diagnostics.getLSPDiagnostics(document.getUri()),
                            document.getVersion()));
        }
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        server.ifShouldNotify(() -> {
            var document = params.getTextDocument();
            var documentUri = document.getUri();
            var documentJavaUri = URI.create(documentUri);
            documents.addDocument(params);
            var hoversFuture = new CompletableFuture<ImmutableSortedMap<Range, MetaNode<?>>>();
            hoverRanges.addHoverRanges(params, hoversFuture);
            CompletableFuture<NamespaceNode<?>> parsingFuture = CompletableFuture.supplyAsync(() -> {
                return withDiagnostics(document, diagnostics -> {
                    var charStream = CharStreams.fromString(document.getText(), documentUri);
                    try {
                        var parser = new Parser(documentJavaUri, diagnostics);
                        var renamer = new Renamer(documentJavaUri, diagnostics, NameEnvironment.withBuiltInNames());
                        var typechecker = new Typechecker(documentJavaUri, diagnostics, TypeEnvironment.withBuiltInTypes());
                        var codegen = new CodeGenerator();
                        var parsed = parser.parse(charStream);
                        var rangeVisitor = new SyntaxNodeRangeVisitor();
                        if (diagnostics.getDiagnostics().isEmpty()) {
                            var renamed = renamer.rename(parsed);
                            if (diagnostics.getDiagnostics().isEmpty()) {
                                var typed = typechecker.typecheck(renamed);
                                typed.accept(rangeVisitor);
                                hoversFuture.complete(rangeVisitor.getRangeNodes());
                                codegen.generate(storagePath, typed);
                                return typed;
                            } else {
                                renamed.accept(rangeVisitor);
                                hoversFuture.complete(rangeVisitor.getRangeNodes());
                                return renamed;
                            }
                        } else {
                            parsed.accept(rangeVisitor);
                            hoversFuture.complete(rangeVisitor.getRangeNodes());
                            return parsed;
                        }
                    } catch (Exception e) {
                        logger.error("Exception while processing syntax tree", e);
                        hoversFuture.completeExceptionally(e);
                        throw e;
                    }
                });
            }, server.getExecutor());
            syntaxTrees.addSyntaxTree(params, parsingFuture);
        });
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        server.ifShouldNotify(() -> {
            var documentUri = params.getTextDocument().getUri();
            var documentJavaUri = URI.create(documentUri);
            var updatedDocument = documents.updateDocument(params);
            var hoversFuture = new CompletableFuture<ImmutableSortedMap<Range, MetaNode<?>>>();
            hoverRanges.updateHoverRanges(params, hoversFuture);
            CompletableFuture<NamespaceNode<?>> parsingFuture = CompletableFuture.supplyAsync(() -> {
                return withDiagnostics(updatedDocument, diagnostics -> {
                    var charStream = CharStreams.fromString(updatedDocument.getText(), documentUri);
                    try {
                        var parser = new Parser(documentJavaUri, diagnostics);
                        var renamer = new Renamer(documentJavaUri, diagnostics, NameEnvironment.withBuiltInNames());
                        var typechecker = new Typechecker(documentJavaUri, diagnostics, TypeEnvironment.withBuiltInTypes());
                        var codegen = new CodeGenerator();
                        var parsed = parser.parse(charStream);
                        var rangeVisitor = new SyntaxNodeRangeVisitor();
                        if (diagnostics.getDiagnostics().isEmpty()) {
                            var renamed = renamer.rename(parsed);
                            if (diagnostics.getDiagnostics().isEmpty()) {
                                var typed = typechecker.typecheck(renamed);
                                typed.accept(rangeVisitor);
                                hoversFuture.complete(rangeVisitor.getRangeNodes());
                                codegen.generate(storagePath, typed);
                                return typed;
                            } else {
                                renamed.accept(rangeVisitor);
                                hoversFuture.complete(rangeVisitor.getRangeNodes());
                                return renamed;
                            }
                        } else {
                            parsed.accept(rangeVisitor);
                            hoversFuture.complete(rangeVisitor.getRangeNodes());
                            return parsed;
                        }
                    } catch (Exception e) {
                        logger.error("Exception while processing syntax tree", e);
                        hoversFuture.completeExceptionally(e);
                        throw e;
                    }
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

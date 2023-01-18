package org.mina_lang.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.eclipse.collections.api.factory.Maps;
import org.jgrapht.alg.cycle.TiernanSimpleCycles;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.mina_lang.codegen.jvm.CodeGenerator;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.parser.ANTLRDiagnosticCollector;
import org.mina_lang.parser.Parser;
import org.mina_lang.renamer.NameEnvironment;
import org.mina_lang.renamer.Renamer;
import org.mina_lang.syntax.NamespaceNode;
import org.mina_lang.typechecker.TypeEnvironment;
import org.mina_lang.typechecker.Typechecker;
import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class Main {
    private ANTLRDiagnosticCollector diagnosticsCollector;

    private Scheduler parScheduler = Schedulers.parallel();
    private Scheduler ioScheduler = Schedulers.boundedElastic();

    private ConcurrentHashMap<NamespaceName, NamespaceNode<?>> namespaceNodes;

    private DOTExporter<NamespaceName, DefaultEdge> dotExporter = new DOTExporter<>();

    public Main(ANTLRDiagnosticCollector diagnostics) {
        this.diagnosticsCollector = diagnostics;
        this.namespaceNodes = new ConcurrentHashMap<>();
        dotExporter.setVertexAttributeProvider(nsName -> Maps.mutable.of(
                "label", DefaultAttribute.createAttribute(nsName.canonicalName())));
    }

    public ANTLRDiagnosticCollector getDiagnosticsCollector() {
        return diagnosticsCollector;
    }

    boolean matchRegularMinaFile(Path filePath, BasicFileAttributes fileAttrs) {
        var fileName = filePath.getFileName().toString();
        return fileName.endsWith(".mina") && fileAttrs.isRegularFile();
    }

    Stream<Path> findMinaFiles(Path startPath) throws IOException {
        return Files.find(
                startPath,
                Integer.MAX_VALUE,
                this::matchRegularMinaFile,
                FileVisitOption.FOLLOW_LINKS);
    }

    Publisher<Path> createPathStream(Path startPath) {
        return Flux.using(
                () -> findMinaFiles(startPath),
                Flux::fromStream,
                Stream::close)
                .subscribeOn(ioScheduler);
    }

    Flux<Path> pathStreamFrom(Path... sourcePaths) {
        return Flux.just(sourcePaths)
                .flatMap(this::createPathStream);
    }

    CharStream readFileContent(Path filePath) throws IOException {
        try (var channel = Files.newByteChannel(filePath)) {
            return CharStreams.fromChannel(
                    channel,
                    StandardCharsets.UTF_8,
                    4096,
                    CodingErrorAction.REPORT,
                    filePath.toUri().toString(),
                    Files.size(filePath));
        }
    }

    public CompletableFuture<Void> compilePath(Path... sourcePaths) throws IOException {
        return pathStreamFrom(sourcePaths)
                .parallel()
                .flatMap(filePath -> {
                    return Mono
                            .fromCallable(() -> readFileContent(filePath))
                            .subscribeOn(ioScheduler);
                })
                .runOn(parScheduler)
                .map(source -> {
                    var sourceUri = URI.create(source.getSourceName());
                    return new Parser(sourceUri, diagnosticsCollector).parse(source);
                })
                .doOnNext(namespaceNode -> {
                    var namespaceName = namespaceNode.id().getName();
                    namespaceNodes.put(namespaceName, namespaceNode);
                })
                .then()
                .doOnSuccess(v -> {
                    var namespaceGraph = GraphTypeBuilder.<NamespaceName, DefaultEdge>directed()
                            .edgeClass(DefaultEdge.class)
                            .allowingMultipleEdges(false)
                            .allowingSelfLoops(false)
                            .buildGraph();

                    namespaceNodes.keySet().forEach(namespaceName -> {
                        namespaceGraph.addVertex(namespaceName);
                    });

                    namespaceGraph.vertexSet()
                            .forEach(namespaceName -> {
                                namespaceNodes.get(namespaceName)
                                        .imports()
                                        .forEach(imp -> {
                                            var importName = imp.namespace().getName();
                                            if (namespaceGraph.containsVertex(importName)) {
                                                namespaceGraph.addVertex(imp.namespace().getName());
                                                namespaceGraph.addEdge(namespaceName, importName);
                                            } else {
                                            }
                                        });
                            });

                    new TiernanSimpleCycles<>(namespaceGraph)
                            .findSimpleCycles()
                            .forEach(cycle -> {
                                System.err.println("Cyclic dependencies found: " + cycle);
                            });

                    dotExporter.exportGraph(namespaceGraph, new PrintWriter(System.out));
                })
                .toFuture();
    }

    public CompletableFuture<NamespaceNode<?>> compileNamespace(
            Path destination,
            CharStream source) {
        return Mono.fromSupplier(() -> {
            var documentUri = URI.create(source.getSourceName());
            var parser = new Parser(documentUri, diagnosticsCollector);
            var parsed = parser.parse(source);
            if (diagnosticsCollector.getDiagnostics().isEmpty()) {
                var renamer = new Renamer(documentUri, diagnosticsCollector, NameEnvironment.withBuiltInNames());
                var renamed = renamer.rename(parsed);
                if (diagnosticsCollector.getDiagnostics().isEmpty()) {
                    var typechecker = new Typechecker(documentUri, diagnosticsCollector,
                            TypeEnvironment.withBuiltInTypes());
                    var typed = typechecker.typecheck(renamed);
                    if (diagnosticsCollector.getDiagnostics().isEmpty()) {
                        var codegen = new CodeGenerator();
                        codegen.generate(destination, typed);
                    }
                    return typed;
                } else {
                    return renamed;
                }
            } else {
                return parsed;
            }
        }).subscribeOn(parScheduler).toFuture();
    }
}

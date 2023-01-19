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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.eclipse.collections.api.factory.Maps;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.mina_lang.codegen.jvm.CodeGenerator;
import org.mina_lang.common.Range;
import org.mina_lang.common.diagnostics.BaseDiagnosticCollector;
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
    private BaseDiagnosticCollector mainCollector;

    private Scheduler parScheduler = Schedulers.parallel();
    private Scheduler ioScheduler = Schedulers.boundedElastic();

    private ConcurrentHashMap<NamespaceName, NamespaceNode<?>> namespaceNodes;
    private ConcurrentHashMap<NamespaceName, ANTLRDiagnosticCollector> scopedDiagnostics;

    private DOTExporter<NamespaceName, DefaultEdge> dotExporter = new DOTExporter<>();

    public Main(BaseDiagnosticCollector diagnostics) {
        this.mainCollector = diagnostics;
        this.namespaceNodes = new ConcurrentHashMap<>();
        this.scopedDiagnostics = new ConcurrentHashMap<>();
        dotExporter.setVertexAttributeProvider(nsName -> Maps.mutable.of(
                "label", DefaultAttribute.createAttribute(nsName.canonicalName())));
    }

    public BaseDiagnosticCollector getMainCollector() {
        return mainCollector;
    }

    void cyclicFileDependency(ANTLRDiagnosticCollector collector, Range range, NamespaceName startNamespace,
            List<NamespaceName> cycle) {
        var messageHeader = "Cyclic namespace dependency found, starting with namespace "
                + startNamespace.canonicalName();

        var namespaceCycleMessage = cycle.stream()
                .<String>reduce("", (concatenatedMessage, namespaceName) -> {
                    if (concatenatedMessage.isEmpty()) {
                        return namespaceName.canonicalName();
                    } else {
                        return concatenatedMessage +
                                ", which imports" +
                                System.lineSeparator() +
                                namespaceName.canonicalName();
                    }
                }, (l, r) -> l + r);

        collector.reportError(
                range,
                messageHeader + ":\n\n" + namespaceCycleMessage);
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
                .doOnNext(source -> {
                    var sourceUri = URI.create(source.getSourceName());
                    var scopedCollector = new ANTLRDiagnosticCollector(mainCollector, sourceUri);
                    var parser = new Parser(scopedCollector);
                    var parsed = parser.parse(source);
                    var namespaceName = parsed.id().getName();
                    scopedDiagnostics.put(namespaceName, scopedCollector);
                    namespaceNodes.put(namespaceName, parsed);
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
                                            // TODO: Handle fully-qualified references
                                            var importName = imp.namespace().getName();
                                            if (namespaceGraph.containsVertex(importName)) {
                                                namespaceGraph.addVertex(imp.namespace().getName());
                                                namespaceGraph.addEdge(namespaceName, importName);
                                            } else {
                                                // TODO: find the namespace on the classpath
                                                // or produce an "unknown namespace" error
                                            }
                                        });
                            });

                    new HawickJamesSimpleCycles<>(namespaceGraph)
                            .findSimpleCycles()
                            .forEach(cycle -> {
                                var cycleStart = cycle.get(0);
                                cycle.add(cycleStart);
                                var cycleStartNs = namespaceNodes.get(cycleStart);
                                var collector = scopedDiagnostics.get(cycleStart);
                                cyclicFileDependency(collector, cycleStartNs.range(), cycleStart, cycle);
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
            var scopedCollector = new ANTLRDiagnosticCollector(mainCollector, documentUri);
            var parser = new Parser(scopedCollector);
            var parsed = parser.parse(source);
            if (!mainCollector.hasErrors()) {
                var renamer = new Renamer(scopedCollector, NameEnvironment.withBuiltInNames());
                var renamed = renamer.rename(parsed);
                if (!mainCollector.hasErrors()) {
                    var typechecker = new Typechecker(scopedCollector, TypeEnvironment.withBuiltInTypes());
                    var typed = typechecker.typecheck(renamed);
                    if (!mainCollector.hasErrors()) {
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

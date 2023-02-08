package org.mina_lang.main;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.eclipse.collections.api.factory.Maps;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.mina_lang.codegen.jvm.CodeGenerator;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencastsoftware.yvette.Range;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    private BaseDiagnosticCollector mainCollector;

    private ConcurrentHashMap<NamespaceName, NamespaceNode<Void>> namespaceNodes;
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
        var cycleMessage = new StringBuilder();

        cycle.forEach(namespaceName -> {
            if (cycleMessage.isEmpty()) {
                cycleMessage.append("Cyclic namespace dependency found, starting with namespace ");
                cycleMessage.append(startNamespace.canonicalName() + ":" + System.lineSeparator().repeat(2));
                cycleMessage.append(namespaceName.canonicalName());
            } else {
                cycleMessage.append(
                        ", which imports" +
                                System.lineSeparator() +
                                namespaceName.canonicalName());
            }
        });

        collector.reportError(range, cycleMessage.toString());
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
                .subscribeOn(Schedulers.boundedElastic());
    }

    Flux<Path> pathStreamFrom(Path... sourcePaths) {
        return Flux.just(sourcePaths)
                .flatMap(this::createPathStream);
    }

    CharStream readFileContent(Path filePath) throws IOException {
        logger.info("Reading {}", filePath.toUri().toString());
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

    public ParallelFlux<CharStream> readSourceData(Path... sourcePaths) {
        return pathStreamFrom(sourcePaths)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(filePath -> {
                    return Mono
                            .fromCallable(() -> readFileContent(filePath))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .runOn(Schedulers.parallel());
    }

    public Graph<NamespaceName, DefaultEdge> constructNamespaceGraph(
            ConcurrentHashMap<NamespaceName, NamespaceNode<Void>> namespaceNodes) {
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
                                    namespaceGraph.addVertex(importName);
                                    namespaceGraph.addEdge(importName, namespaceName);
                                } else {
                                    // TODO: find the namespace on the classpath
                                    // or produce an "unknown namespace" error
                                }
                            });
                });

        // TODO: Figure out why JohnsonSimpleCycles throws exception
        new HawickJamesSimpleCycles<>(namespaceGraph)
                .findSimpleCycles()
                .forEach(cycle -> {
                    Collections.reverse(cycle);
                    var cycleStart = cycle.get(0);
                    cycle.add(cycleStart);
                    var cycleStartNs = namespaceNodes.get(cycleStart);
                    var collector = scopedDiagnostics.get(cycleStart);
                    var cyclicImport = cycleStartNs.imports().detect(imp -> {
                        var cycleNext = cycle.get(1);
                        return imp.namespace().getName().equals(cycleNext);
                    });
                    cyclicFileDependency(collector, cyclicImport.range(), cycleStart, cycle);
                });

        return namespaceGraph;
    }

    public CompletableFuture<Void> compileSourcePaths(Path destinationPath, Path... sourcePaths) throws IOException {
        var sourceData = readSourceData(sourcePaths);

        var parsingPhase = new ParsingPhase(sourceData, scopedDiagnostics, namespaceNodes, mainCollector);

        return Phase.sequenceMono(parsingPhase, namespaceNodes -> {
            var namespaceGraph = constructNamespaceGraph(namespaceNodes);

            // We can't proceed if our namespace graph is badly formed
            if (mainCollector.hasErrors()) {
                return Mono.empty();
            } else {
                var renamingPhase = new RenamingPhase(namespaceGraph, namespaceNodes, scopedDiagnostics);

                var typecheckingPhase = Phase.sequence(renamingPhase, renamedNodes -> {
                    return new TypecheckingPhase(namespaceGraph, renamedNodes, scopedDiagnostics);
                });

                var codegenPhase = Phase.sequence(typecheckingPhase, typecheckedNodes -> {
                    return new CodegenPhase(destinationPath, typecheckedNodes, scopedDiagnostics);
                });

                return Phase.runMono(codegenPhase);
            }
        }).then().toFuture();
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
        }).subscribeOn(Schedulers.parallel()).toFuture();
    }
}

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
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.mina_lang.codegen.jvm.CodeGenerator;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Range;
import org.mina_lang.common.diagnostics.BaseDiagnosticCollector;
import org.mina_lang.common.names.Name;
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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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

    public CompletableFuture<Void> compileSourcePaths(Path destinationPath, Path... sourcePaths) throws IOException {
        return pathStreamFrom(sourcePaths)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(filePath -> {
                    return Mono
                            .fromCallable(() -> readFileContent(filePath))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .runOn(Schedulers.parallel())
                .doOnNext(source -> {
                    logger.info("Parsing {}", source.getSourceName());
                    var sourceUri = URI.create(source.getSourceName());
                    var scopedCollector = new ANTLRDiagnosticCollector(mainCollector, sourceUri);
                    var parser = new Parser(scopedCollector);
                    var parsed = parser.parse(source);
                    var namespaceName = parsed.id().getName();
                    scopedDiagnostics.put(namespaceName, scopedCollector);
                    namespaceNodes.put(namespaceName, parsed);
                })
                .then()
                .concatWith(Mono.defer(() -> {
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
                                    return imp.namespace().pkg().equals(cycleNext.pkg()) &&
                                            imp.namespace().ns().equals(cycleNext.name());
                                });
                                cyclicFileDependency(collector, cyclicImport.range(), cycleStart, cycle);
                            });

                    // We can't proceed if our namespace graph is badly formed
                    if (mainCollector.hasErrors()) {
                        return Mono.empty();
                    } else {
                        var renamingPhase = new NamespaceGraphTraversal<Void, Name>(parsed -> {
                            var nsName = parsed.id().getName();
                            var nsDiagnostics = scopedDiagnostics.get(nsName);
                            logger.info("Renaming namespace {}", nsName.canonicalName());
                            var renamer = new Renamer(nsDiagnostics, NameEnvironment.withBuiltInNames());
                            var renamed = renamer.rename(parsed);
                            return Mono.just(renamed);
                        }, namespaceGraph, namespaceNodes, scopedDiagnostics);

                        return Flux.concat(renamingPhase.topoTraverse(), Mono.defer(() -> {
                            var typecheckingPhase = new NamespaceGraphTraversal<Name, Attributes>(renamed -> {
                                var nsName = renamed.id().getName();
                                var nsDiagnostics = scopedDiagnostics.get(nsName);
                                logger.info("Typechecking namespace {}", nsName.canonicalName());
                                var typechecker = new Typechecker(nsDiagnostics, TypeEnvironment.withBuiltInTypes());
                                var typechecked = typechecker.typecheck(renamed);
                                return Mono.just(typechecked);
                            }, namespaceGraph, renamingPhase.getTransformedNodes(), scopedDiagnostics);

                            return Flux.concat(typecheckingPhase.topoTraverse(), Mono.defer(() -> {
                                return Flux.fromIterable(typecheckingPhase.getTransformedNodes().values())
                                        .parallel()
                                        .runOn(Schedulers.boundedElastic())
                                        .doOnNext(typechecked -> {
                                            var nsName = typechecked.id().getName();
                                            var nsDiagnostics = scopedDiagnostics.get(nsName);
                                            if (!nsDiagnostics.hasErrors()) {
                                                logger.info("Emitting namespace {}", nsName.canonicalName());
                                                var codegen = new CodeGenerator();
                                                codegen.generate(destinationPath, typechecked);
                                            }
                                        }).then();
                            })).then();
                        })).then();

                    }
                }))
                .then()
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
        }).subscribeOn(Schedulers.parallel()).toFuture();
    }
}

/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.main;

import com.opencastsoftware.yvette.Range;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.mina_lang.common.diagnostics.BaseDiagnosticCollector;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.parser.ANTLRDiagnosticReporter;
import org.mina_lang.syntax.NamespaceNode;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    private BaseDiagnosticCollector mainCollector;

    private ConcurrentHashMap<NamespaceName, NamespaceNode<Void>> namespaceNodes;
    private ConcurrentHashMap<NamespaceName, ANTLRDiagnosticReporter> scopedDiagnostics;

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

    void cyclicFileDependency(ANTLRDiagnosticReporter collector, Range range, NamespaceName startNamespace,
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

    public void checkForImportCycles(Graph<NamespaceName, DefaultEdge> namespaceGraph) {
        // TODO: Figure out why JohnsonSimpleCycles throws exception
        var cycleDetector = new HawickJamesSimpleCycles<>(namespaceGraph);

        cycleDetector.findSimpleCycles().forEach(importCycle -> {
            Collections.reverse(importCycle);

            // Add the initial namespace to the end to illustrate the cycle
            var startNs = importCycle.get(0);
            importCycle.add(startNs);

            // Find the import statement that triggers the cycle
            var startNsNode = namespaceNodes.get(startNs);
            var cyclicImport = startNsNode.imports().detect(imp -> {
                var nextNs = importCycle.get(1);
                var importedNs = imp.namespace().getName();
                return importedNs.equals(nextNs);
            });

            // Report the import cycle at the appropriate location
            var startNsCollector = scopedDiagnostics.get(startNs);
            cyclicFileDependency(startNsCollector, cyclicImport.range(), startNs, importCycle);
        });
    }

    public Graph<NamespaceName, DefaultEdge> constructNamespaceGraph(
        ConcurrentHashMap<NamespaceName, NamespaceNode<Void>> namespaceNodes,
        Set<NamespaceName> importedNamespaces) {
        var namespaceGraph = GraphTypeBuilder.<NamespaceName, DefaultEdge>directed()
                .edgeClass(DefaultEdge.class)
                .allowingMultipleEdges(false)
                .allowingSelfLoops(false)
                .buildGraph();

        namespaceNodes.keySet().forEach(namespaceGraph::addVertex);

        namespaceNodes.forEach((namespaceName, namespaceNode) -> {
            namespaceNode
                .imports()
                .forEach(imp -> {
                    var importName = imp.namespace().getName();
                    if (namespaceGraph.containsVertex(importName)) {
                        namespaceGraph.addVertex(importName);
                        namespaceGraph.addEdge(importName, namespaceName);
                    } else {
                        importedNamespaces.add(importName);
                    }
                });
        });

        checkForImportCycles(namespaceGraph);

        return namespaceGraph;
    }

    public CompletableFuture<Void> compileSourcePaths(URL[] classpath, Path destinationPath, Path... sourcePaths) throws IOException {
        var sourceData = readSourceData(sourcePaths);

        var parsingPhase = new ParsingPhase(sourceData, scopedDiagnostics, namespaceNodes, mainCollector);

        return parsingPhase.runPhase().flatMap(parsedNodes -> {
            Set<NamespaceName> importedNamespaces = Sets.mutable.empty();
            var namespaceGraph = constructNamespaceGraph(parsedNodes, importedNamespaces);

            // We can't proceed if our namespace graph is badly formed
            if (mainCollector.hasErrors()) {
                return Mono.empty();
            } else {
                return Mono.using(() -> new URLClassLoader(classpath), classLoader -> {
                    var classpathResolutionPhase = new ClasspathResolutionPhase(classLoader, importedNamespaces);

                    return classpathResolutionPhase.runPhase().flatMap(classpathScopes -> {

                        var renamingPhase = new RenamingPhase(namespaceGraph, classpathScopes, parsedNodes, scopedDiagnostics);

                        var typecheckingPhase = Phase.andThen(renamingPhase, renamedNodes -> {
                            return new TypecheckingPhase(namespaceGraph, classpathScopes, renamedNodes, scopedDiagnostics);
                        });

                        var codegenPhase = Phase.andThen(typecheckingPhase, typecheckedNodes -> {
                            return new CodegenPhase(destinationPath, typecheckedNodes, scopedDiagnostics);
                        });

                        return Phase.runMono(codegenPhase);
                    });
                }, classLoader -> {
                    try { classLoader.close(); }
                    catch (IOException e) { throw Exceptions.propagate(e); }
                });
            }
        }).then().toFuture();
    }
}

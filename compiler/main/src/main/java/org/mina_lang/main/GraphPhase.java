/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.main;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.parser.ANTLRDiagnosticReporter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public non-sealed abstract class GraphPhase<A, B>
        implements
        Phase<ConcurrentHashMap<NamespaceName, B>> {

    protected Graph<NamespaceName, DefaultEdge> namespaceGraph;

    protected ConcurrentHashMap<NamespaceName, A> inputNodes;
    protected ConcurrentHashMap<NamespaceName, B> transformedNodes;

    protected ConcurrentHashMap<NamespaceName, ANTLRDiagnosticReporter> scopedDiagnostics;

    private final Set<NamespaceName> rootNodes;
    private final Map<NamespaceName, AtomicInteger> namespaceDependencies;

    GraphPhase(
            Graph<NamespaceName, DefaultEdge> namespaceGraph,
            ConcurrentHashMap<NamespaceName, A> namespaceNodes,
            ConcurrentHashMap<NamespaceName, ANTLRDiagnosticReporter> scopedDiagnostics) {
        this.namespaceGraph = namespaceGraph;
        this.inputNodes = namespaceNodes;
        this.scopedDiagnostics = scopedDiagnostics;
        this.transformedNodes = new ConcurrentHashMap<>();
        this.rootNodes = new HashSet<>();
        this.namespaceDependencies = new HashMap<>();
        namespaceGraph.vertexSet().forEach(namespace -> {
            var inDegree = namespaceGraph.inDegreeOf(namespace);
            if (inDegree > 0) {
                namespaceDependencies.put(namespace, new AtomicInteger(inDegree));
            } else {
                rootNodes.add(namespace);
            }
        });
    }

    abstract Mono<B> transformNode(A inputNode);

    ParallelFlux<B> topoTraverseFrom(NamespaceName startNode) {
        return Optional.ofNullable(inputNodes.get(startNode))
                .map(inputNode -> {
                    var nsDiagnostics = scopedDiagnostics.get(startNode);
                    if (nsDiagnostics.hasErrors()) {
                        return Mono.<B>empty();
                    } else {
                        return transformNode(inputNode);
                    }
                })
                .orElseGet(Mono::empty) // This may happen if the node had errors in a previous phase
                .doOnNext(transformedNode -> transformedNodes.put(startNode, transformedNode))
                .flatMapMany(transformedNode -> {
                    var nsDiagnostics = scopedDiagnostics.get(startNode);
                    if (nsDiagnostics.hasErrors()) {
                        return Flux.empty();
                    } else {
                        return Flux.fromIterable(Graphs.successorListOf(namespaceGraph, startNode))
                                .parallel()
                                .runOn(Schedulers.parallel())
                                .flatMap(successorNode -> {
                                    var remaining = namespaceDependencies
                                            .get(successorNode)
                                            .decrementAndGet();
                                    if (remaining > 0) {
                                        return Flux.<B>empty().parallel();
                                    } else {
                                        return topoTraverseFrom(successorNode);
                                    }
                                });
                    }
                })
                .parallel()
                .runOn(Schedulers.parallel());
    }

    public Mono<ConcurrentHashMap<NamespaceName, B>> runPhase() {
        return Flux.fromIterable(rootNodes)
                .parallel()
                .runOn(Schedulers.parallel())
                .flatMap(this::topoTraverseFrom)
                .then()
                .thenReturn(transformedData());
    }

    public ConcurrentHashMap<NamespaceName, B> transformedData() {
        return transformedNodes;
    }
}

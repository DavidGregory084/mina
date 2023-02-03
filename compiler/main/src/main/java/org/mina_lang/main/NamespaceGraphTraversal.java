package org.mina_lang.main;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.parser.ANTLRDiagnosticCollector;
import org.mina_lang.syntax.NamespaceNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

class NamespaceGraphTraversal<A, B> {
    private Function<NamespaceNode<A>, Mono<NamespaceNode<B>>> phaseFn;

    private Graph<NamespaceName, DefaultEdge> namespaceGraph;

    private ConcurrentHashMap<NamespaceName, NamespaceNode<A>> inputNodes;
    private ConcurrentHashMap<NamespaceName, NamespaceNode<B>> transformedNodes;

    private ConcurrentHashMap<NamespaceName, ANTLRDiagnosticCollector> scopedDiagnostics;

    private Set<NamespaceName> rootNodes;
    private Map<NamespaceName, AtomicInteger> namespaceDependencies;

    NamespaceGraphTraversal(
            Function<NamespaceNode<A>, Mono<NamespaceNode<B>>> phaseFn,
            Graph<NamespaceName, DefaultEdge> namespaceGraph,
            ConcurrentHashMap<NamespaceName, NamespaceNode<A>> namespaceNodes,
            ConcurrentHashMap<NamespaceName, ANTLRDiagnosticCollector> scopedDiagnostics) {
        this.phaseFn = phaseFn;
        this.namespaceGraph = namespaceGraph;
        this.inputNodes = namespaceNodes;
        this.scopedDiagnostics = scopedDiagnostics;
        this.transformedNodes = new ConcurrentHashMap<>();
        this.rootNodes = Sets.mutable.empty();
        this.namespaceDependencies = Maps.mutable.empty();
        namespaceGraph.vertexSet().forEach(namespace -> {
            var inDegree = namespaceGraph.inDegreeOf(namespace);
            if (inDegree > 0) {
                namespaceDependencies.put(namespace, new AtomicInteger(inDegree));
            } else {
                rootNodes.add(namespace);
            }
        });
    }

    ParallelFlux<NamespaceNode<B>> topoTraverseFrom(NamespaceName startNode) {
        return Optional.ofNullable(inputNodes.get(startNode))
                .map(inputNode -> {
                    var nsName = inputNode.id().getName();
                    var nsDiagnostics = scopedDiagnostics.get(nsName);
                    if (nsDiagnostics.hasErrors()) {
                        return Mono.<NamespaceNode<B>>empty();
                    } else {
                        return phaseFn.apply(inputNode);
                    }
                })
                .orElseGet(() -> Mono.empty()) // This may happen if the node had errors in a previous phase
                .doOnNext(transformedNode -> {
                    transformedNodes.put(startNode, transformedNode);
                })
                .flatMapMany(transformedNode -> {
                    var nsName = transformedNode.id().getName();
                    var nsDiagnostics = scopedDiagnostics.get(nsName);
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
                                        return Flux.<NamespaceNode<B>>empty().parallel();
                                    } else {
                                        return topoTraverseFrom(successorNode);
                                    }
                                });
                    }
                })
                .parallel()
                .runOn(Schedulers.parallel());
    }

    Mono<Void> topoTraverse() {
        return Flux.fromIterable(rootNodes)
                .parallel()
                .runOn(Schedulers.parallel())
                .flatMap(this::topoTraverseFrom)
                .then();
    }

    ConcurrentHashMap<NamespaceName, NamespaceNode<B>> getTransformedNodes() {
        return transformedNodes;
    }
}

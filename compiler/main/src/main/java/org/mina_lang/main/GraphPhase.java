package org.mina_lang.main;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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

public non-sealed abstract class GraphPhase<A, B>
        implements
        Phase<ConcurrentHashMap<NamespaceName, NamespaceNode<B>>> {

    protected Graph<NamespaceName, DefaultEdge> namespaceGraph;

    protected ConcurrentHashMap<NamespaceName, NamespaceNode<A>> inputNodes;
    protected ConcurrentHashMap<NamespaceName, NamespaceNode<B>> transformedNodes;

    protected ConcurrentHashMap<NamespaceName, ANTLRDiagnosticCollector> scopedDiagnostics;

    private final Set<NamespaceName> rootNodes;
    private final Map<NamespaceName, AtomicInteger> namespaceDependencies;

    GraphPhase(
            Graph<NamespaceName, DefaultEdge> namespaceGraph,
            ConcurrentHashMap<NamespaceName, NamespaceNode<A>> namespaceNodes,
            ConcurrentHashMap<NamespaceName, ANTLRDiagnosticCollector> scopedDiagnostics) {
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

    abstract Mono<NamespaceNode<B>> transformNode(NamespaceNode<A> inputNode);

    ParallelFlux<NamespaceNode<B>> topoTraverseFrom(NamespaceName startNode) {
        return Optional.ofNullable(inputNodes.get(startNode))
                .map(inputNode -> {
                    var nsDiagnostics = scopedDiagnostics.get(startNode);
                    if (nsDiagnostics.hasErrors()) {
                        return Mono.<NamespaceNode<B>>empty();
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

    public Mono<Void> runPhase() {
        return Flux.fromIterable(rootNodes)
                .parallel()
                .runOn(Schedulers.parallel())
                .flatMap(this::topoTraverseFrom)
                .then();
    }

    public ConcurrentHashMap<NamespaceName, NamespaceNode<B>> transformedData() {
        return transformedNodes;
    }
}

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
import org.mina_lang.syntax.NamespaceNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

public class NamespaceGraphTraversal<A, B> {
    private Function<NamespaceNode<A>, Mono<NamespaceNode<B>>> phaseFn;

    private Graph<NamespaceName, DefaultEdge> namespaceGraph;

    private ConcurrentHashMap<NamespaceName, NamespaceNode<A>> inputNodes;
    private ConcurrentHashMap<NamespaceName, NamespaceNode<B>> transformedNodes;

    private Set<NamespaceName> rootNodes;
    private Map<NamespaceName, AtomicInteger> namespaceDependencies;

    public NamespaceGraphTraversal(
            Function<NamespaceNode<A>, Mono<NamespaceNode<B>>> phaseFn,
            Graph<NamespaceName, DefaultEdge> namespaceGraph,
            ConcurrentHashMap<NamespaceName, NamespaceNode<A>> namespaceNodes) {
        this.phaseFn = phaseFn;
        this.namespaceGraph = namespaceGraph;
        this.inputNodes = namespaceNodes;
        this.transformedNodes = new ConcurrentHashMap<>();
        this.rootNodes = Sets.mutable.empty();
        this.namespaceDependencies = Maps.mutable.empty();
        namespaceGraph.vertexSet().forEach(namespace -> {
            if (namespaceGraph.inDegreeOf(namespace) == 0) {
                rootNodes.add(namespace);
            } else {
                namespaceDependencies.put(namespace, new AtomicInteger(namespaceGraph.inDegreeOf(namespace)));
            }
        });
    }

    ParallelFlux<NamespaceNode<B>> recurse(NamespaceName startNode) {
        return Optional.ofNullable(inputNodes.get(startNode))
                .map(phaseFn::apply)
                .orElseGet(() -> Mono.empty())
                .doOnNext(processedNamespace -> {
                    transformedNodes.put(startNode, processedNamespace);
                })
                .flatMapMany(processedNamespace -> {
                    return Flux.concat(
                            Flux.just(processedNamespace),
                            Flux.fromIterable(Graphs.successorListOf(namespaceGraph, startNode))
                                    .parallel()
                                    .flatMap(successorNode -> {
                                        var remaining = namespaceDependencies
                                                .get(successorNode)
                                                .decrementAndGet();
                                        var emptyFlux = ParallelFlux.from(Flux.<NamespaceNode<B>>empty());
                                        return remaining > 0 ? emptyFlux : recurse(successorNode);
                                    }));
                })
                .parallel();
    }

    public Mono<Void> traverse() {
        return Flux.fromIterable(rootNodes)
                .parallel()
                .runOn(Schedulers.parallel())
                .flatMap(this::recurse)
                .then();
    }

    public ConcurrentHashMap<NamespaceName, NamespaceNode<B>> getTransformedNodes() {
        return transformedNodes;
    }
}

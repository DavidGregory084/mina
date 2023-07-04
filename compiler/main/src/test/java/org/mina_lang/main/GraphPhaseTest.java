package org.mina_lang.main;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.mina_lang.syntax.SyntaxNodes.*;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

import org.eclipse.collections.api.factory.Lists;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.mina_lang.common.diagnostics.BaseDiagnosticCollector;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.parser.ANTLRDiagnosticCollector;
import org.mina_lang.syntax.NamespaceNode;

import com.opencastsoftware.yvette.Range;

import net.jqwik.api.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class GraphPhaseTest {
    private Random random = new Random();

    private BaseDiagnosticCollector baseCollector = new BaseDiagnosticCollector() {
    };

    @Property(tries = 100)
    void graphTraversalIsTopological(@ForAll("namespaceGraph") DirectedAcyclicGraph<NamespaceName, DefaultEdge> graph) {
        var visited = new ConcurrentLinkedQueue<NamespaceName>();
        var namespaceNodes = new ConcurrentHashMap<NamespaceName, NamespaceNode<Void>>();
        var scopedDiagnostics = new ConcurrentHashMap<NamespaceName, ANTLRDiagnosticCollector>();

        graph.vertexSet().forEach(nsName -> {
            var sourceURI = URI.create("Mina/Test/Main/" + nsName.name() + ".mina");
            var predecessors = Graphs.predecessorListOf(graph, nsName);
            // Technically pointless, but let's create imports mirroring our namespace graph
            var imports = Lists.immutable.ofAll(predecessors).collect(importedNs -> {
                var importIdNode = nsIdNode(Range.EMPTY, importedNs.pkg(), importedNs.name());
                return importNode(Range.EMPTY, importIdNode);
            });
            scopedDiagnostics.put(
                    nsName,
                    new ANTLRDiagnosticCollector(baseCollector, sourceURI));
            namespaceNodes.put(
                    nsName,
                    namespaceNode(
                            Range.EMPTY,
                            nsIdNode(Range.EMPTY, nsName.pkg(), nsName.name()),
                            imports,
                            Lists.immutable.empty()));
        });

        var phase = new GraphPhase<Void, Void>(graph, namespaceNodes, scopedDiagnostics) {
            @Override
            Mono<NamespaceNode<Void>> transformNode(NamespaceNode<Void> inputNode) {
                visited.add(inputNode.getName());
                return Mono
                    .delay(Duration.ofMillis(random.nextLong(20)))
                    .map(delay -> inputNode);
            }
        };

        StepVerifier.create(phase.runPhase())
                .expectNext(namespaceNodes)
                .expectComplete()
                .verify();

        var visitedList = Lists.mutable.ofAll(visited);

        visitedList.forEachWithIndex((visitedNode, visitedIndex) -> {
            graph.getAncestors(visitedNode).forEach(ancestor -> {
                assertThat(ancestor, visitedIndex(visitedList, lessThan(visitedIndex)));
            });
        });
    }

    private FeatureMatcher<NamespaceName, Integer> visitedIndex(List<NamespaceName> visitedList,
            Matcher<Integer> matcher) {
        return new FeatureMatcher<NamespaceName, Integer>(matcher, "A namespace with visited index", "visited index") {
            @Override
            protected Integer featureValueOf(NamespaceName actual) {
                return visitedList.indexOf(actual);
            }
        };
    }

    @Provide
    private Arbitrary<DirectedAcyclicGraph<NamespaceName, DefaultEdge>> namespaceGraph() {
        return Arbitraries.recursive(
                () -> namespaceName().map(startNs -> {
                    return DirectedAcyclicGraph
                            .<NamespaceName, DefaultEdge>createBuilder(DefaultEdge.class)
                            .addVertex(startNs)
                            .build();
                }), this::addNamespace, 0, 10);
    }

    private Arbitrary<NamespaceName> namespaceName() {
        return Arbitraries
                .strings()
                .alpha()
                .ofMaxLength(10)
                .map(ident -> new NamespaceName(
                        Lists.immutable.of("Mina", "Test", "Main"),
                        ident));
    }

    private Arbitrary<DirectedAcyclicGraph<NamespaceName, DefaultEdge>> addNamespace(
            Arbitrary<DirectedAcyclicGraph<NamespaceName, DefaultEdge>> existing) {
        return existing.flatMap(graph -> {
            return namespaceName()
                    .filter(newNs -> !graph.containsVertex(newNs))
                    .flatMap(newNs -> {
                        return existingVertex(graph).list().map(ancestors -> {
                            graph.addVertex(newNs);

                            ancestors.forEach(ancestorNs -> {
                                graph.addEdge(ancestorNs, newNs);
                            });

                            return graph;
                        });
                    });
        });
    }

    private Arbitrary<NamespaceName> existingVertex(DirectedAcyclicGraph<NamespaceName, DefaultEdge> graph) {
        return Arbitraries.of(Set.copyOf(graph.vertexSet()));
    }
}

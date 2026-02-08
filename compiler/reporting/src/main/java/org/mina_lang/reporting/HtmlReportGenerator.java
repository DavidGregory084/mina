/*
 * SPDX-FileCopyrightText:  © 2026 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.reporting;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.resolver.ClasspathResolver;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.ina.Namespace;
import org.mina_lang.syntax.NamespaceNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class HtmlReportGenerator implements ReportGenerator {
    private final MustacheFactory mustacheFactory;
    private final Path reportDir;
    private final Path indexPage;

    private final AtomicReference<Graph<NamespaceName, DefaultEdge>> namespaceGraph;

    public HtmlReportGenerator(Path reportDir) {
        this.mustacheFactory = new DefaultMustacheFactory(new ClasspathResolver());
        this.reportDir = reportDir;
        this.indexPage = reportDir.resolve("index.html");
        this.namespaceGraph = new AtomicReference<>();
    }

    private Path namespacePath(NamespaceName namespaceName) {
        Path namespacePath = reportDir;

        for (var pkg : namespaceName.pkg()) {
            namespacePath = namespacePath.resolve(pkg);
        }

        return namespacePath.resolve(namespaceName.name() + ".html");
    }

    @Override
    public void reportNamespaceGraph(Graph<NamespaceName, DefaultEdge> graph) {
        namespaceGraph.set(graph);
        try {
            writeIndexPage(graph);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void reportNamespace(NamespaceNode<?> namespaceNode) {
    }

    @Override
    public void reportIntermediate(Namespace namespace) {

    }

    private void writeIndexPage(Graph<NamespaceName, DefaultEdge> namespaceGraph) throws IOException {
        var mustache = mustacheFactory.compile("index.mustache");
        var namespaceToPath = namespaceGraph.vertexSet().stream()
            .sorted(Comparator.comparing(NamespaceName::canonicalName))
            .map(ns -> Map.entry(ns, reportDir.relativize(namespacePath(ns))))
            .toList();
        try (var writer = Files.newBufferedWriter(indexPage)) {
            mustache.execute(writer, Map.of("namespaces", namespaceToPath));
        }
    }
}

/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.main;

import com.sourcegraph.Scip.*;
import org.mina_lang.BuildInfo;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.indexer.Indexer;
import org.mina_lang.parser.ANTLRDiagnosticCollector;
import org.mina_lang.syntax.NamespaceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public class IndexerPhase implements ParallelPhase<NamespaceNode<Attributes>, Void> {
    private static final Logger logger = LoggerFactory.getLogger(CodegenPhase.class);

    private final Path indexPath;
    private final ConcurrentHashMap<NamespaceName, NamespaceNode<Attributes>> namespaceNodes;
    private final ConcurrentHashMap<NamespaceName, ANTLRDiagnosticCollector> scopedDiagnostics;

    private final Index.Builder builder;

    public IndexerPhase(Path destinationPath,
                        ConcurrentHashMap<NamespaceName, NamespaceNode<Attributes>> namespaceNodes,
                        ConcurrentHashMap<NamespaceName, ANTLRDiagnosticCollector> scopedDiagnostics) {
        this.indexPath = destinationPath.resolve("index.scip");
        this.namespaceNodes = namespaceNodes;
        this.scopedDiagnostics = scopedDiagnostics;
        this.builder = Index.newBuilder();
        builder.setMetadata(
            Metadata
                .newBuilder()
                .setVersionValue(ProtocolVersion.UnspecifiedProtocolVersion.getNumber())
                .setTextDocumentEncoding(TextEncoding.UTF8)
                .setToolInfo(ToolInfo.newBuilder()
                    .setName("minac")
                    .setVersion(BuildInfo.version)));
    }

    @Override
    public ParallelFlux<NamespaceNode<Attributes>> inputFlux() {
        return Flux.fromIterable(namespaceNodes.values())
            .parallel()
            .runOn(Schedulers.parallel());
    }

    @Override
    public void consumeInput(NamespaceNode<Attributes> typecheckedNode) {
        var nsName = typecheckedNode.id().getName();
        var nsDiagnostics = scopedDiagnostics.get(nsName);
        logger.info("Indexing namespace {}", nsName.canonicalName());
        var indexer = new Indexer(nsDiagnostics);
        var document = indexer.index(typecheckedNode);
        builder.addDocuments(document);
    }

    @Override
    public Void transformedData() throws IOException {
        var index = builder.build();
        Files.write(indexPath, index.toByteArray());
        return null;
    }
}

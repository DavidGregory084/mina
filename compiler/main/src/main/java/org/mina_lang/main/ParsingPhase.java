/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.main;

import org.antlr.v4.runtime.CharStream;
import org.mina_lang.common.diagnostics.BaseDiagnosticCollector;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.parser.ANTLRDiagnosticReporter;
import org.mina_lang.parser.Parser;
import org.mina_lang.syntax.NamespaceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.ParallelFlux;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

public class ParsingPhase implements ParallelPhase<CharStream, ConcurrentHashMap<NamespaceName, NamespaceNode<Void>>> {
    private static final Logger logger = LoggerFactory.getLogger(ParsingPhase.class);

    private final ParallelFlux<CharStream> sourceFileData;
    private final ConcurrentHashMap<NamespaceName, ANTLRDiagnosticReporter> scopedDiagnostics;
    private final ConcurrentHashMap<NamespaceName, NamespaceNode<Void>> transformedNodes;
    private final BaseDiagnosticCollector mainCollector;

    public ParsingPhase(ParallelFlux<CharStream> sourceFileData,
            ConcurrentHashMap<NamespaceName, ANTLRDiagnosticReporter> scopedDiagnostics,
            ConcurrentHashMap<NamespaceName, NamespaceNode<Void>> transformedNodes,
            BaseDiagnosticCollector mainCollector) {
        this.sourceFileData = sourceFileData;
        this.scopedDiagnostics = scopedDiagnostics;
        this.transformedNodes = transformedNodes;
        this.mainCollector = mainCollector;
    }

    @Override
    public ParallelFlux<CharStream> inputFlux() {
        return sourceFileData;
    }

    @Override
    public void consumeInput(CharStream source) {
        logger.info("Parsing {}", source.getSourceName());
        var sourceUri = URI.create(source.getSourceName());
        var scopedCollector = new ANTLRDiagnosticReporter(mainCollector, sourceUri);
        var parser = new Parser(scopedCollector);
        var parsed = parser.parse(source);
        var namespaceName = parsed.id().getName();
        scopedDiagnostics.put(namespaceName, scopedCollector);
        transformedNodes.put(namespaceName, parsed);
    }

    @Override
    public ConcurrentHashMap<NamespaceName, NamespaceNode<Void>> transformedData() {
        return transformedNodes;
    }
}

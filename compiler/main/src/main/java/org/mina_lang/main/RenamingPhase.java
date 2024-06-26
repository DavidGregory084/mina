/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.main;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.parser.ANTLRDiagnosticReporter;
import org.mina_lang.renamer.NameEnvironment;
import org.mina_lang.renamer.Renamer;
import org.mina_lang.syntax.NamespaceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

public class RenamingPhase extends GraphPhase<Void, Name> {
    private static final Logger logger = LoggerFactory.getLogger(RenamingPhase.class);

    public RenamingPhase(
            Graph<NamespaceName, DefaultEdge> namespaceGraph,
            ConcurrentHashMap<NamespaceName, NamespaceNode<Void>> namespaceNodes,
            ConcurrentHashMap<NamespaceName, ANTLRDiagnosticReporter> scopedDiagnostics) {
        super(namespaceGraph, namespaceNodes, scopedDiagnostics);
    }

    @Override
    Mono<NamespaceNode<Name>> transformNode(NamespaceNode<Void> parsedNode) {
        var nsName = parsedNode.id().getName();
        var nsDiagnostics = scopedDiagnostics.get(nsName);
        logger.info("Renaming namespace {}", nsName.canonicalName());
        var renamer = new Renamer(nsDiagnostics, NameEnvironment.withBuiltInNames());
        var renamedNode = renamer.rename(parsedNode);
        return Mono.just(renamedNode);
    }
}

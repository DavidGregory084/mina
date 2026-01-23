/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.main;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.names.LocalName;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.common.names.SyntheticNameSupply;
import org.mina_lang.ina.InaNodePrinter;
import org.mina_lang.ina.Namespace;
import org.mina_lang.optimiser.ConstantPropagation;
import org.mina_lang.optimiser.Lower;
import org.mina_lang.parser.ANTLRDiagnosticReporter;
import org.mina_lang.syntax.NamespaceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

public class OptimiserPhase extends GraphPhase<NamespaceNode<Attributes>, Namespace> {
    private static final Logger logger = LoggerFactory.getLogger(OptimiserPhase.class);
    private static final InaNodePrinter printer = new InaNodePrinter();

    public OptimiserPhase(
        Graph<NamespaceName, DefaultEdge> namespaceGraph,
        ConcurrentHashMap<NamespaceName, NamespaceNode<Attributes>> namespaceNodes,
        ConcurrentHashMap<NamespaceName, ANTLRDiagnosticReporter> scopedDiagnostics) {
        super(namespaceGraph, namespaceNodes, scopedDiagnostics);
    }

    @Override
    Mono<Namespace> transformNode(NamespaceNode<Attributes> typecheckedNode) {
        var nsName = typecheckedNode.id().getName();
        logger.info("Optimising namespace {}", nsName.canonicalName());
        var nameSupply = new SyntheticNameSupply();
        var lower = new Lower(nameSupply);
        var lowered = lower.lower(typecheckedNode);
        logger.info("Before folding:\n{}", lowered.accept(printer).render());
        var constants = new ConstantPropagation();
        var constantFolded = constants.optimiseNamespace(lowered);
        logger.info("After folding:\n{}", constantFolded.accept(printer).render());
        constants.getEnvironment().entrySet().forEach(constant -> {
            logger.info(
                "{} -> {}",
                constant.getKey() instanceof LocalName local
                    ? local.name() + "@" + local.index()
                    : constant.getKey().canonicalName(),
                constant.getValue()
            );
        });
        return Mono.just(constantFolded);
    }
}

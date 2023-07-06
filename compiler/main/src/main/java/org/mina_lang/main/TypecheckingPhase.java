/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.main;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.parser.ANTLRDiagnosticCollector;
import org.mina_lang.syntax.NamespaceNode;
import org.mina_lang.typechecker.TypeEnvironment;
import org.mina_lang.typechecker.Typechecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

public class TypecheckingPhase extends GraphPhase<Name, Attributes> {
    private static final Logger logger = LoggerFactory.getLogger(TypecheckingPhase.class);

    public TypecheckingPhase(
            Graph<NamespaceName, DefaultEdge> namespaceGraph,
            ConcurrentHashMap<NamespaceName, NamespaceNode<Name>> namespaceNodes,
            ConcurrentHashMap<NamespaceName, ANTLRDiagnosticCollector> scopedDiagnostics) {
        super(namespaceGraph, namespaceNodes, scopedDiagnostics);
    }

    @Override
    Mono<NamespaceNode<Attributes>> transformNode(NamespaceNode<Name> renamedNode) {
        var nsName = renamedNode.id().getName();
        var nsDiagnostics = scopedDiagnostics.get(nsName);
        logger.info("Typechecking namespace {}", nsName.canonicalName());
        var typechecker = new Typechecker(nsDiagnostics, TypeEnvironment.withBuiltInTypes());
        var typecheckedNode = typechecker.typecheck(renamedNode);
        return Mono.just(typecheckedNode);
    }
}

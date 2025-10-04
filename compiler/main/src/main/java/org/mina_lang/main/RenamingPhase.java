/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.main;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Scope;
import org.mina_lang.common.diagnostics.NamespaceDiagnosticReporter;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.names.Named;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.parser.ANTLRDiagnosticReporter;
import org.mina_lang.renamer.NameEnvironment;
import org.mina_lang.renamer.Renamer;
import org.mina_lang.renamer.scopes.ImportedNamesScope;
import org.mina_lang.syntax.NamespaceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RenamingPhase extends GraphPhase<NamespaceNode<Void>, NamespaceNode<Name>> implements ImportScopePopulation<Name, ImportedNamesScope> {
    private static final Logger logger = LoggerFactory.getLogger(RenamingPhase.class);

    private final Map<NamespaceName, Scope<Meta<Attributes>>> classpathScopes;

    public RenamingPhase(
            Graph<NamespaceName, DefaultEdge> namespaceGraph,
            Map<NamespaceName, Scope<Meta<Attributes>>> classpathScopes,
            ConcurrentHashMap<NamespaceName, NamespaceNode<Void>> namespaceNodes,
            ConcurrentHashMap<NamespaceName, ANTLRDiagnosticReporter> scopedDiagnostics) {
        super(namespaceGraph, namespaceNodes, scopedDiagnostics);
        this.classpathScopes = classpathScopes;
    }

    @Override
    public Optional<NamespaceNode<Name>> getNamespaceNode(NamespaceName namespaceName) {
        return Optional.ofNullable(transformedNodes.get(namespaceName));
    }

    @Override
    public NamespaceDiagnosticReporter getNamespaceDiagnostics(NamespaceName namespaceName) {
        return scopedDiagnostics.get(namespaceName);
    }

    @Override
    public Optional<Scope<Meta<Attributes>>> getClasspathScope(NamespaceName namespaceName) {
        return Optional.ofNullable(classpathScopes.get(namespaceName));
    }

    @Override
    public Named getName(Meta<Name> meta) {
        return (Named) meta.meta();
    }

    @Override
    public Meta<Name> transformMeta(Meta<Attributes> meta) {
        return new Meta<>(meta.range(), meta.meta().name());
    }

    @Override
    Mono<NamespaceNode<Name>> transformNode(NamespaceNode<Void> parsedNode) {
        var nsName = parsedNode.id().getName();
        var nsDiagnostics = scopedDiagnostics.get(nsName);

        var renamedNode = populateImportScope(parsedNode, new ImportedNamesScope())
            .map(importScope -> {
                logger.info("Renaming namespace {}", nsName.canonicalName());
                var nameEnvironment = NameEnvironment.withBuiltInNames();
                nameEnvironment.pushScope(importScope);
                var renamer = new Renamer(nsDiagnostics, nameEnvironment);
                return renamer.rename(parsedNode);
            });

        return Mono.justOrEmpty(renamedNode);
    }
}

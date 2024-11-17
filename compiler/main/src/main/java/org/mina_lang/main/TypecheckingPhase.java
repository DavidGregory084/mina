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
import org.mina_lang.syntax.NamespaceNode;
import org.mina_lang.typechecker.TypeEnvironment;
import org.mina_lang.typechecker.Typechecker;
import org.mina_lang.typechecker.scopes.ImportedTypesScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TypecheckingPhase extends GraphPhase<Name, Attributes> implements ImportScopePopulation<Attributes, ImportedTypesScope> {
    private static final Logger logger = LoggerFactory.getLogger(TypecheckingPhase.class);

    private final Map<NamespaceName, Scope<Attributes>> classpathScopes;

    public TypecheckingPhase(
        Graph<NamespaceName, DefaultEdge> namespaceGraph,
        Map<NamespaceName, Scope<Attributes>> classpathScopes,
        ConcurrentHashMap<NamespaceName, NamespaceNode<Name>> namespaceNodes,
        ConcurrentHashMap<NamespaceName, ANTLRDiagnosticReporter> scopedDiagnostics) {
        super(namespaceGraph, namespaceNodes, scopedDiagnostics);
        this.classpathScopes = classpathScopes;
    }

    @Override
    public Optional<NamespaceNode<Attributes>> getNamespaceNode(NamespaceName namespaceName) {
        return Optional.ofNullable(transformedNodes.get(namespaceName));
    }

    @Override
    public NamespaceDiagnosticReporter getNamespaceDiagnostics(NamespaceName namespaceName) {
        return scopedDiagnostics.get(namespaceName);
    }

    @Override
    public Optional<Scope<Attributes>> getClasspathScope(NamespaceName namespaceName) {
        return Optional.ofNullable(classpathScopes.get(namespaceName));
    }

    @Override
    public Named getName(Meta<Attributes> meta) {
        return (Named) meta.meta().name();
    }

    @Override
    public Meta<Attributes> transformMeta(Meta<Attributes> meta) {
        return meta;
    }

    @Override
    public Scope<Attributes> transformScope(Scope<Attributes> scope) {
        return scope;
    }

    @Override
    Mono<NamespaceNode<Attributes>> transformNode(NamespaceNode<Name> renamedNode) {
        var nsName = renamedNode.id().getName();
        var nsDiagnostics = scopedDiagnostics.get(nsName);

        var typecheckedNode = populateImportScope(renamedNode, new ImportedTypesScope())
            .map(importScope -> {
                logger.info("Typechecking namespace {}", nsName.canonicalName());
                var typeEnvironment = TypeEnvironment.withBuiltInTypes();
                typeEnvironment.pushScope(importScope);
                var typechecker = new Typechecker(nsDiagnostics, typeEnvironment);
                return typechecker.typecheck(renamedNode);
            });

        return Mono.justOrEmpty(typecheckedNode);
    }
}

/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.main;

import org.mina_lang.codegen.jvm.CodeGenerator;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.parser.ANTLRDiagnosticReporter;
import org.mina_lang.syntax.NamespaceNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public class CodegenPhase implements ParallelPhase<NamespaceNode<Attributes>, Void> {
    private static final Logger logger = LoggerFactory.getLogger(CodegenPhase.class);

    private final Path destinationPath;
    private final ConcurrentHashMap<NamespaceName, NamespaceNode<Attributes>> namespaceNodes;
    private final ConcurrentHashMap<NamespaceName, ANTLRDiagnosticReporter> scopedDiagnostics;

    public CodegenPhase(Path destinationPath,
            ConcurrentHashMap<NamespaceName, NamespaceNode<Attributes>> namespaceNodes,
            ConcurrentHashMap<NamespaceName, ANTLRDiagnosticReporter> scopedDiagnostics) {
        this.destinationPath = destinationPath;
        this.namespaceNodes = namespaceNodes;
        this.scopedDiagnostics = scopedDiagnostics;
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
        if (!nsDiagnostics.hasErrors()) {
            logger.info("Emitting namespace {}", nsName.canonicalName());
            var codegen = new CodeGenerator();
            codegen.generate(destinationPath, typecheckedNode);
        }
    }

    @Override
    public Void transformedData() {
        return null;
    }
}

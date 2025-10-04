/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.main;

import org.mina_lang.codegen.jvm.NamespaceReader;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Scope;
import org.mina_lang.common.names.NamespaceName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClasspathResolutionPhase implements ParallelPhase<NamespaceName, ConcurrentHashMap<NamespaceName, Scope<Meta<Attributes>>>> {
    private static final Logger logger = LoggerFactory.getLogger(ClasspathResolutionPhase.class);

    private final ParallelFlux<NamespaceName> importedNamespaces;
    private final ConcurrentHashMap<NamespaceName, Scope<Meta<Attributes>>> classpathScopes = new ConcurrentHashMap<>();

    private final ClassLoader classLoader;

    public ClasspathResolutionPhase(ClassLoader classLoader, Set<NamespaceName> importedNamespaces) {
        this.classLoader = classLoader;
        this.importedNamespaces = Flux
            .fromIterable(importedNamespaces)
            .parallel();
    }

    @Override
    public ParallelFlux<NamespaceName> inputFlux() {
        return importedNamespaces;
    }

    @Override
    public void consumeInput(NamespaceName importedNamespace) throws IOException {
        logger.info("Resolving imported namespace {}", importedNamespace.canonicalName());
        var classpathScope = NamespaceReader.readScope(classLoader, importedNamespace);
        if (classpathScope != null) {
            classpathScopes.put(importedNamespace, classpathScope);
        }
    }

    @Override
    public ConcurrentHashMap<NamespaceName, Scope<Meta<Attributes>>> transformedData() {
        return classpathScopes;
    }
}

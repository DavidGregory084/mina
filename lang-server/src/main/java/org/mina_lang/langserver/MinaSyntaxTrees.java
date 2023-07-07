/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver;

import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.mina_lang.syntax.NamespaceNode;

import java.util.concurrent.CompletableFuture;

public class MinaSyntaxTrees {
    private final ConcurrentHashMap<String, CompletableFuture<NamespaceNode<?>>> syntaxTrees = new ConcurrentHashMap<>();

    public CompletableFuture<NamespaceNode<?>> get(String uri) {
        return syntaxTrees.get(uri);
    }

    public void addSyntaxTree(DidOpenTextDocumentParams params, CompletableFuture<NamespaceNode<?>> treeFuture) {
        syntaxTrees.put(params.getTextDocument().getUri(), treeFuture);
    }

    public void updateSyntaxTree(DidChangeTextDocumentParams params, CompletableFuture<NamespaceNode<?>> treeFuture) {
        syntaxTrees.put(params.getTextDocument().getUri(), treeFuture);
    }

    public void removeSyntaxTree(DidCloseTextDocumentParams params) {
        syntaxTrees.remove(params.getTextDocument().getUri());
    }
}

package org.mina_lang.langserver;

import java.util.concurrent.CompletableFuture;

import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.mina_lang.common.Attributes;
import org.mina_lang.syntax.NamespaceNode;

public class MinaSyntaxTrees {
    private final ConcurrentHashMap<String, CompletableFuture<NamespaceNode<Attributes>>> syntaxTrees = new ConcurrentHashMap<>();

    public CompletableFuture<NamespaceNode<Attributes>> get(String uri) {
        return syntaxTrees.get(uri);
    }

    public void addSyntaxTree(DidOpenTextDocumentParams params, CompletableFuture<NamespaceNode<Attributes>> treeFuture) {
        syntaxTrees.put(params.getTextDocument().getUri(), treeFuture);
    }

    public void updateSyntaxTree(DidChangeTextDocumentParams params, CompletableFuture<NamespaceNode<Attributes>> treeFuture) {
        syntaxTrees.put(params.getTextDocument().getUri(), treeFuture);
    }

    public void removeSyntaxTree(DidCloseTextDocumentParams params) {
        syntaxTrees.remove(params.getTextDocument().getUri());
    }
}

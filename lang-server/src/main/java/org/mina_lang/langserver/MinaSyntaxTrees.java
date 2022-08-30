package org.mina_lang.langserver;

import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.mina_lang.syntax.NamespaceNode;

import java.util.concurrent.CompletableFuture;

public class MinaSyntaxTrees {
    private final ConcurrentHashMap<String, CompletableFuture<NamespaceNode<Void>>> syntaxTrees = new ConcurrentHashMap<>();

    public CompletableFuture<NamespaceNode<Void>> get(String uri) {
        return syntaxTrees.get(uri);
    }

    public void addSyntaxTree(DidOpenTextDocumentParams params, CompletableFuture<NamespaceNode<Void>> parsingFuture) {
        syntaxTrees.put(params.getTextDocument().getUri(), parsingFuture);
    }

    public void updateSyntaxTree(DidChangeTextDocumentParams params, CompletableFuture<NamespaceNode<Void>> parsingFuture) {
        syntaxTrees.put(params.getTextDocument().getUri(), parsingFuture);
    }

    public void removeSyntaxTree(DidCloseTextDocumentParams params) {
        syntaxTrees.remove(params.getTextDocument().getUri());
    }
}

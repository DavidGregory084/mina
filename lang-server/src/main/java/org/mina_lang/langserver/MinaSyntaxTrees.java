package org.mina_lang.langserver;

import java.util.concurrent.CompletableFuture;

import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.mina_lang.syntax.CompilationUnitNode;

public class MinaSyntaxTrees {
    private final ConcurrentHashMap<String, CompletableFuture<CompilationUnitNode>> syntaxTrees = new ConcurrentHashMap<>();

    public CompletableFuture<CompilationUnitNode> get(String uri) {
        return syntaxTrees.get(uri);
    }

    public void addSyntaxTree(DidOpenTextDocumentParams params, CompletableFuture<CompilationUnitNode> parsingFuture) {
        syntaxTrees.put(params.getTextDocument().getUri(), parsingFuture);
    }

    public void updateSyntaxTree(DidChangeTextDocumentParams params, CompletableFuture<CompilationUnitNode> parsingFuture) {
        syntaxTrees.put(params.getTextDocument().getUri(), parsingFuture);
    }

    public void removeSyntaxTree(DidCloseTextDocumentParams params) {
        syntaxTrees.remove(params.getTextDocument().getUri());
    }
}

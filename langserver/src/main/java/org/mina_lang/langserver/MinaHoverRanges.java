package org.mina_lang.langserver;

import java.util.concurrent.CompletableFuture;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.Range;
import org.mina_lang.syntax.SyntaxNode;

public class MinaHoverRanges {
    private final ConcurrentHashMap<String, CompletableFuture<ImmutableList<Pair<Range, SyntaxNode>>>> hoverRanges = new ConcurrentHashMap<>();

    public CompletableFuture<ImmutableList<Pair<Range, SyntaxNode>>> get(String uri) {
        return hoverRanges.get(uri);
    }

    public void addHoverRanges(DidOpenTextDocumentParams params, CompletableFuture<ImmutableList<Pair<Range, SyntaxNode>>> ranges) {
        hoverRanges.put(params.getTextDocument().getUri(), ranges);
    }

    public void updateHoverRanges(DidChangeTextDocumentParams params, CompletableFuture<ImmutableList<Pair<Range, SyntaxNode>>> ranges) {
        hoverRanges.put(params.getTextDocument().getUri(), ranges);
    }

    public void removeHoverRanges(DidCloseTextDocumentParams params) {
        hoverRanges.remove(params.getTextDocument().getUri());
    }
}

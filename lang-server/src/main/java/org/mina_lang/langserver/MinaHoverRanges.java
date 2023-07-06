/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.util.Ranges;
import org.mina_lang.syntax.SyntaxNode;

import java.util.concurrent.CompletableFuture;

public class MinaHoverRanges {
    private final ConcurrentHashMap<String, CompletableFuture<ImmutableSortedMap<Range, SyntaxNode>>> hoverRanges = new ConcurrentHashMap<>();

    public CompletableFuture<ImmutableSortedMap<Range, SyntaxNode>> get(String uri) {
        return hoverRanges.get(uri);
    }

    public CompletableFuture<Pair<Range, SyntaxNode>> get(String uri, Position hoverPos) {
        var documentRanges = hoverRanges.get(uri);
        return documentRanges == null ? CompletableFuture.completedFuture(null)
                : hoverRanges.get(uri).thenApply(ranges -> {
                    return ranges.detect((range, node) -> Ranges.containsPosition(range, hoverPos));
                });
    }

    public void addHoverRanges(DidOpenTextDocumentParams params,
            CompletableFuture<ImmutableSortedMap<Range, SyntaxNode>> ranges) {
        hoverRanges.put(params.getTextDocument().getUri(), ranges);
    }

    public void updateHoverRanges(DidChangeTextDocumentParams params,
            CompletableFuture<ImmutableSortedMap<Range, SyntaxNode>> ranges) {
        hoverRanges.put(params.getTextDocument().getUri(), ranges);
    }

    public void removeHoverRanges(DidCloseTextDocumentParams params) {
        hoverRanges.remove(params.getTextDocument().getUri());
    }
}

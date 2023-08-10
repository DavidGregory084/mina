/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver.documents;

import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;

import java.util.regex.Pattern;

public class TextDocument {
    private static final Pattern LINE_ENDINGS = Pattern.compile("(\\r\\n|\\r|\\n)");

    private TextDocument() {
    }

    private static int[] getLineIndices(String documentText) {
        var indices = IntLists.mutable.empty();
        var matcher = LINE_ENDINGS.matcher(documentText);

        indices.add(0);
        while (matcher.find()) {
            indices.add(matcher.end());
        }

        return indices.toArray();
    }

    private static int positionToIndex(Position position, int[] lineIndices) {
        var lineIndex = lineIndices[position.getLine()];
        return lineIndex + position.getCharacter();
    }

    private static String applyContentChange(String oldText, TextDocumentContentChangeEvent change) {
        var range = change.getRange();
        var newText = change.getText();

        var lineIndices = getLineIndices(oldText);

        var startIndex = positionToIndex(range.getStart(), lineIndices);
        var endIndex = positionToIndex(range.getEnd(), lineIndices);

        var prefix = oldText.substring(0, startIndex);
        var suffix = oldText.substring(endIndex);

        return prefix + newText + suffix;
    }

    public static TextDocumentItem applyChanges(TextDocumentItem document, DidChangeTextDocumentParams params) {
        var newVersion = params.getTextDocument().getVersion();
        var changes = params.getContentChanges();
        var documentText = document.getText();

        for (var change : changes) {
            documentText = applyContentChange(documentText, change);
        }

        return new TextDocumentItem(
                document.getUri(), document.getLanguageId(),
                newVersion, documentText);
    }
}

package org.mina_lang.langserver;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

public class MinaDiagnosticCollector extends BaseErrorListener {

    MutableList<Diagnostic> diagnostics = Lists.mutable.empty();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
            String msg, RecognitionException e) {
        var startPos = new Position(line - 1, charPositionInLine);

        Position endPos;
        if (offendingSymbol instanceof Token token) {
            endPos = new Position(line - 1, charPositionInLine + token.getText().length());
        } else {
            endPos = startPos;
        }

        var range = new Range(startPos, endPos);

        diagnostics.add(new Diagnostic(range, msg, DiagnosticSeverity.Error, "mina"));
    }

    public MutableList<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
}

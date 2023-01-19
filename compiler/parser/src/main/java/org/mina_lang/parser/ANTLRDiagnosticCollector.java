package org.mina_lang.parser;

import java.net.URI;
import java.util.BitSet;
import java.util.List;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Location;
import org.mina_lang.common.Position;
import org.mina_lang.common.Range;
import org.mina_lang.common.diagnostics.BaseDiagnosticCollector;
import org.mina_lang.common.diagnostics.Diagnostic;
import org.mina_lang.common.diagnostics.DiagnosticRelatedInformation;
import org.mina_lang.common.diagnostics.ScopedDiagnosticCollector;

public class ANTLRDiagnosticCollector implements ScopedDiagnosticCollector, ANTLRErrorListener {

    private BaseDiagnosticCollector parent;
    private URI sourceUri;

    public ANTLRDiagnosticCollector(BaseDiagnosticCollector parent, URI sourceUri) {
        this.parent = parent;
        this.sourceUri = sourceUri;

    }

    @Override
    public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact,
            BitSet ambigAlts, ATNConfigSet configs) {
    }

    @Override
    public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
            BitSet conflictingAlts, ATNConfigSet configs) {
    }

    @Override
    public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction,
            ATNConfigSet configs) {
    }

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

        reportError(range, msg);
    }

    @Override
    public List<Diagnostic> getDiagnostics() {
        return parent.getDiagnostics();
    }

    @Override
    public void reportError(Location location, String message) {
        parent.reportError(location, message);
    }

    @Override
    public void reportError(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        parent.reportError(location, message, relatedInformation);
    }

    @Override
    public void reportWarning(Location location, String message) {
        parent.reportWarning(location, message);
    }

    @Override
    public void reportWarning(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        parent.reportWarning(location, message, relatedInformation);
    }

    @Override
    public void reportInfo(Location location, String message) {
        parent.reportInfo(location, message);
    }

    @Override
    public void reportInfo(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        parent.reportInfo(location, message, relatedInformation);
    }

    @Override
    public void reportHint(Location location, String message) {
        parent.reportHint(location, message);
    }

    @Override
    public void reportHint(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        parent.reportHint(location, message, relatedInformation);
    }

    @Override
    public URI getSourceUri() {
        return sourceUri;
    }
}

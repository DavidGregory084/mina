package org.mina_lang.parser;

import java.net.URI;
import java.util.BitSet;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.mina_lang.common.diagnostics.BaseDiagnosticCollector;
import org.mina_lang.common.diagnostics.ForwardingDiagnosticCollector;

import com.opencastsoftware.yvette.Position;
import com.opencastsoftware.yvette.Range;

public class ANTLRDiagnosticCollector extends ForwardingDiagnosticCollector implements ANTLRErrorListener {

    public ANTLRDiagnosticCollector(BaseDiagnosticCollector parent, URI sourceUri) {
        super(parent, sourceUri);
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
}

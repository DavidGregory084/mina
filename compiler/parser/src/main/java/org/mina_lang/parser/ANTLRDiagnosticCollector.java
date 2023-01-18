package org.mina_lang.parser;

import java.net.URI;
import java.util.BitSet;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.mina_lang.common.Location;
import org.mina_lang.common.Position;
import org.mina_lang.common.Range;
import org.mina_lang.common.diagnostics.BaseDiagnosticCollector;

public abstract class ANTLRDiagnosticCollector extends BaseDiagnosticCollector implements ANTLRErrorListener {
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

        if (recognizer instanceof Lexer lexer) {
            var sourceName = lexer.getInputStream().getSourceName();
            if (!CharStream.UNKNOWN_SOURCE_NAME.equals(sourceName)) {
                var location = new Location(URI.create(sourceName), range);
                reportError(location, msg);
            }
        } else if (recognizer instanceof Parser parser) {
            var sourceName = parser.getInputStream().getSourceName();
            if (!CharStream.UNKNOWN_SOURCE_NAME.equals(sourceName)) {
                var location = new Location(URI.create(sourceName), range);
                reportError(location, msg);
            }
        }
    }
}

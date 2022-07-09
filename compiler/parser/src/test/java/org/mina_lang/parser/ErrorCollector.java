package org.mina_lang.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import java.util.List;

public class ErrorCollector extends BaseErrorListener {
    private MutableList<String> errors = Lists.mutable.empty();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
            String msg, RecognitionException e) {
        errors.add(msg);
    }

    public List<String> getErrors() {
        return errors;
    }
}

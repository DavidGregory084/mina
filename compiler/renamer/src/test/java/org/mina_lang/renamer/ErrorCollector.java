package org.mina_lang.renamer;

import java.util.List;
import java.util.stream.Collectors;

import org.mina_lang.common.diagnostics.BaseDiagnosticCollector;

public class ErrorCollector extends BaseDiagnosticCollector {
    public List<String> getErrors() {
        return getDiagnostics().stream()
            .map(diagnostic -> diagnostic.message())
            .collect(Collectors.toList());
    }
}


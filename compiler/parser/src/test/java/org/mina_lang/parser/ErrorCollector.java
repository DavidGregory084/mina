package org.mina_lang.parser;

import java.util.List;
import java.util.stream.Collectors;

public class ErrorCollector extends ANTLRDiagnosticCollector {
    public List<String> getErrors() {
        return getDiagnostics().stream()
            .map(diagnostic -> diagnostic.message())
            .collect(Collectors.toList());
    }
}


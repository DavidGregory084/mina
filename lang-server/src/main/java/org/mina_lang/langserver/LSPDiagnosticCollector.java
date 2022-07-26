package org.mina_lang.langserver;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.mina_lang.common.DiagnosticCollector;

public interface LSPDiagnosticCollector extends DiagnosticCollector {
    default List<Diagnostic> getLSPDiagnostics() {
        return getDiagnostics().stream()
                .map(diagnostic -> {
                    var start = diagnostic.range().start();
                    var end = diagnostic.range().end();
                    var range = new Range(
                            new Position(start.line(), start.character()),
                            new Position(end.line(), end.character()));
                    var severity = DiagnosticSeverity.forValue(diagnostic.severity().getCode());
                    return new Diagnostic(range, diagnostic.message(), severity, "mina");
                })
                .collect(Collectors.toList());
    }
}

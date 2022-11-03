package org.mina_lang.langserver;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.*;
import org.mina_lang.common.diagnostics.DiagnosticCollector;

public interface LSPDiagnosticCollector extends DiagnosticCollector {
    default List<Diagnostic> getLSPDiagnostics(String uri) {
        return getDiagnostics().stream()
                .map(diagnostic -> {
                    var start = diagnostic.range().start();
                    var end = diagnostic.range().end();
                    var range = new Range(
                            new Position(start.line(), start.character()),
                            new Position(end.line(), end.character()));
                    var severity = DiagnosticSeverity.forValue(diagnostic.severity().getCode());
                    var relatedInformation = diagnostic.relatedInformation().stream().map(related -> {
                        var relatedStart = related.range().start();
                        var relatedEnd = related.range().end();
                        var relatedRange = new Range(
                                new Position(relatedStart.line(), relatedStart.character()),
                                new Position(relatedEnd.line(), relatedEnd.character()));
                        var relatedLoc = new Location(uri, relatedRange);
                        return new DiagnosticRelatedInformation(relatedLoc, related.message());
                    }).collect(Collectors.toList());
                    var lspDiagnostic = new Diagnostic(range, diagnostic.message(), severity, "mina");
                    lspDiagnostic.setRelatedInformation(relatedInformation);
                    return lspDiagnostic;
                })
                .collect(Collectors.toList());
    }
}

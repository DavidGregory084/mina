package org.mina_lang.langserver;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.*;
import org.mina_lang.common.diagnostics.DiagnosticCollector;

public interface LSPDiagnosticCollector extends DiagnosticCollector {
    default List<Diagnostic> getLSPDiagnostics(String uri) {
        return getDiagnostics().stream()
                .distinct()
                .map(diagnostic -> {
                    var start = diagnostic.location().range().start();
                    var end = diagnostic.location().range().end();
                    var range = new Range(
                            new Position(start.line(), start.character()),
                            new Position(end.line(), end.character()));
                    var severity = DiagnosticSeverity.forValue(diagnostic.severity().getCode());
                    var relatedInformation = diagnostic.relatedInformation().stream().map(related -> {
                        var relatedUri = related.location().uri();
                        var relatedStart = related.location().range().start();
                        var relatedEnd = related.location().range().end();
                        var relatedRange = new Range(
                                new Position(relatedStart.line(), relatedStart.character()),
                                new Position(relatedEnd.line(), relatedEnd.character()));
                        var relatedLoc = new Location(relatedUri.toString(), relatedRange);
                        return new DiagnosticRelatedInformation(relatedLoc, related.message());
                    }).collect(Collectors.toList());
                    var lspDiagnostic = new Diagnostic(range, diagnostic.message(), severity, "mina");
                    lspDiagnostic.setRelatedInformation(relatedInformation);
                    return lspDiagnostic;
                })
                .collect(Collectors.toList());
    }
}

package org.mina_lang.common;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class BaseDiagnosticCollector implements DiagnosticCollector {
    ConcurrentLinkedQueue<Diagnostic> diagnostics = new ConcurrentLinkedQueue<>();

    public List<Diagnostic> getDiagnostics() {
        return diagnostics.stream().toList();
    }

    @Override
    public void reportError(Range range, String message) {
        diagnostics.offer(new Diagnostic(range, DiagnosticSeverity.Error, message));
    }

    @Override
    public void reportWarning(Range range, String message) {
        diagnostics.offer(new Diagnostic(range, DiagnosticSeverity.Warning, message));
    }

    @Override
    public void reportInfo(Range range, String message) {
        diagnostics.offer(new Diagnostic(range, DiagnosticSeverity.Information, message));
    }

    @Override
    public void reportHint(Range range, String message) {
        diagnostics.offer(new Diagnostic(range, DiagnosticSeverity.Hint, message));
    }
}

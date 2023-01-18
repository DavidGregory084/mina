package org.mina_lang.common.diagnostics;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.*;

public abstract class BaseDiagnosticCollector implements DiagnosticCollector {
    ConcurrentLinkedQueue<Diagnostic> diagnostics = new ConcurrentLinkedQueue<>();

    public List<Diagnostic> getDiagnostics() {
        return diagnostics.stream().toList();
    }

    @Override
    public void reportError(Location location, String message) {
        diagnostics.offer(new Diagnostic(location, DiagnosticSeverity.Error, message));
    }

    @Override
    public void reportError(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        diagnostics.offer(new Diagnostic(location, DiagnosticSeverity.Error, message, relatedInformation));
    }

    @Override
    public void reportWarning(Location location, String message) {
        diagnostics.offer(new Diagnostic(location, DiagnosticSeverity.Warning, message));
    }

    @Override
    public void reportWarning(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        diagnostics.offer(new Diagnostic(location, DiagnosticSeverity.Warning, message, relatedInformation));
    }

    @Override
    public void reportInfo(Location location, String message) {
        diagnostics.offer(new Diagnostic(location, DiagnosticSeverity.Information, message));
    }

    @Override
    public void reportInfo(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        diagnostics.offer(new Diagnostic(location, DiagnosticSeverity.Information, message, relatedInformation));
    }

    @Override
    public void reportHint(Location location, String message) {
        diagnostics.offer(new Diagnostic(location, DiagnosticSeverity.Hint, message));
    }

    @Override
    public void reportHint(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        diagnostics.offer(new Diagnostic(location, DiagnosticSeverity.Hint, message, relatedInformation));
    }
}

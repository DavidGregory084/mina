package org.mina_lang.common.diagnostics;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.*;

public abstract class BaseDiagnosticCollector implements DiagnosticCollector {
    ConcurrentLinkedQueue<Diagnostic> diagnostics = new ConcurrentLinkedQueue<>();
    AtomicInteger errorCount = new AtomicInteger(0);
    AtomicInteger warningCount = new AtomicInteger(0);

    public List<Diagnostic> getDiagnostics() {
        return diagnostics.stream().toList();
    }

    @Override
    public void reportError(Location location, String message) {
        diagnostics.offer(new Diagnostic(location, DiagnosticSeverity.Error, message));
        errorCount.incrementAndGet();
    }

    @Override
    public void reportError(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        diagnostics.offer(new Diagnostic(location, DiagnosticSeverity.Error, message, relatedInformation));
        errorCount.incrementAndGet();
    }

    @Override
    public void reportWarning(Location location, String message) {
        diagnostics.offer(new Diagnostic(location, DiagnosticSeverity.Warning, message));
        warningCount.incrementAndGet();
    }

    @Override
    public void reportWarning(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        diagnostics.offer(new Diagnostic(location, DiagnosticSeverity.Warning, message, relatedInformation));
        warningCount.incrementAndGet();
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

    @Override
    public int errorCount() {
        return errorCount.get();
    }

    @Override
    public int warningCount() {
        return warningCount.get();
    }

    @Override
    public boolean hasErrors() {
        return errorCount() > 0;
    }

    @Override
    public boolean hasWarnings() {
        return warningCount() > 0;
    }
}

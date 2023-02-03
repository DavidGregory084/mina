package org.mina_lang.common.diagnostics;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Location;

import com.opencastsoftware.yvette.Severity;

public class ForwardingDiagnosticCollector implements ScopedDiagnosticCollector {

    ConcurrentLinkedQueue<Diagnostic> diagnostics = new ConcurrentLinkedQueue<>();
    AtomicInteger errorCount = new AtomicInteger(0);
    AtomicInteger warningCount = new AtomicInteger(0);

    private BaseDiagnosticCollector parent;
    private URI sourceUri;

    public ForwardingDiagnosticCollector(BaseDiagnosticCollector parent, URI sourceUri) {
        this.parent = parent;
        this.sourceUri = sourceUri;
    }

    @Override
    public List<Diagnostic> getDiagnostics() {
        return parent.getDiagnostics();
    }

    @Override
    public void reportError(Location location, String message) {
        diagnostics.offer(new Diagnostic(location, Severity.Error, message));
        errorCount.incrementAndGet();
        parent.reportError(location, message);
    }

    @Override
    public void reportError(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        diagnostics.offer(new Diagnostic(location, Severity.Error, message, relatedInformation));
        errorCount.incrementAndGet();
        parent.reportError(location, message, relatedInformation);
    }

    @Override
    public void reportWarning(Location location, String message) {
        diagnostics.offer(new Diagnostic(location, Severity.Warning, message));
        warningCount.incrementAndGet();
        parent.reportWarning(location, message);
    }

    @Override
    public void reportWarning(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        diagnostics.offer(new Diagnostic(location, Severity.Warning, message, relatedInformation));
        warningCount.incrementAndGet();
        parent.reportWarning(location, message, relatedInformation);
    }

    @Override
    public void reportInfo(Location location, String message) {
        diagnostics.offer(new Diagnostic(location, Severity.Information, message));
        parent.reportInfo(location, message);
    }

    @Override
    public void reportInfo(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        diagnostics.offer(new Diagnostic(location, Severity.Information, message, relatedInformation));
        parent.reportInfo(location, message, relatedInformation);
    }

    @Override
    public void reportHint(Location location, String message) {
        diagnostics.offer(new Diagnostic(location, Severity.Hint, message));
        parent.reportHint(location, message);
    }

    @Override
    public void reportHint(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        diagnostics.offer(new Diagnostic(location, Severity.Hint, message, relatedInformation));
        parent.reportHint(location, message, relatedInformation);
    }

    @Override
    public URI getSourceUri() {
        return sourceUri;
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

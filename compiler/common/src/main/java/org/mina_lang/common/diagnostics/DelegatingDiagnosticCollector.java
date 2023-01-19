package org.mina_lang.common.diagnostics;

import java.net.URI;
import java.util.List;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Location;

public class DelegatingDiagnosticCollector implements ScopedDiagnosticCollector {

    private BaseDiagnosticCollector parent;
    private URI sourceUri;

    public DelegatingDiagnosticCollector(BaseDiagnosticCollector parent, URI sourceUri) {
        this.parent = parent;
        this.sourceUri = sourceUri;
    }

    @Override
    public List<Diagnostic> getDiagnostics() {
        return parent.getDiagnostics();
    }

    @Override
    public void reportError(Location location, String message) {
        parent.reportError(location, message);
    }

    @Override
    public void reportError(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        parent.reportError(location, message, relatedInformation);
    }

    @Override
    public void reportWarning(Location location, String message) {
        parent.reportWarning(location, message);
    }

    @Override
    public void reportWarning(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        parent.reportWarning(location, message, relatedInformation);
    }

    @Override
    public void reportInfo(Location location, String message) {
        parent.reportInfo(location, message);
    }

    @Override
    public void reportInfo(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        parent.reportInfo(location, message, relatedInformation);
    }

    @Override
    public void reportHint(Location location, String message) {
        parent.reportHint(location, message);
    }

    @Override
    public void reportHint(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        parent.reportHint(location, message, relatedInformation);
    }

    @Override
    public URI getSourceUri() {
        return sourceUri;
    }

    @Override
    public int errorCount() {
        return parent.errorCount();
    }

    @Override
    public int warningCount() {
        return parent.warningCount();
    }

    @Override
    public boolean hasErrors() {
        return parent.hasErrors();
    }

    @Override
    public boolean hasWarnings() {
        return parent.hasWarnings();
    }
}

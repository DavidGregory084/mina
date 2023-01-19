package org.mina_lang.common.diagnostics;

import java.net.URI;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Location;
import org.mina_lang.common.Range;

public interface ScopedDiagnosticCollector extends DiagnosticCollector {

    public URI getSourceUri();

    default public void reportError(Range range, String message) {
        reportError(new Location(getSourceUri(), range), message);
    }

    default public void reportError(Range range, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        reportError(new Location(getSourceUri(), range), message, relatedInformation);
    }

    default public void reportWarning(Range range, String message) {
        reportWarning(new Location(getSourceUri(), range), message);
    }

    default public void reportWarning(Range range, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        reportWarning(new Location(getSourceUri(), range), message, relatedInformation);
    }

    default public void reportInfo(Range range, String message) {
        reportInfo(new Location(getSourceUri(), range), message);
    }

    default public void reportInfo(Range range, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        reportInfo(new Location(getSourceUri(), range), message, relatedInformation);
    }

    default public void reportHint(Range range, String message) {
        reportInfo(new Location(getSourceUri(), range), message);
    }

    default public void reportHint(Range range, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        reportInfo(new Location(getSourceUri(), range), message, relatedInformation);
    }
}

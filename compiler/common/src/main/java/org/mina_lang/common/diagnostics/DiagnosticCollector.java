package org.mina_lang.common.diagnostics;

import java.util.List;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Location;

public interface DiagnosticCollector {
    public List<Diagnostic> getDiagnostics();

    public void reportError(Location location, String message);

    public void reportError(Location location, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation);

    public void reportWarning(Location location, String message);

    public void reportWarning(Location location, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation);

    public void reportInfo(Location location, String message);

    public void reportInfo(Location location, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation);

    public void reportHint(Location location, String message);

    public void reportHint(Location location, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation);
}

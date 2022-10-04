package org.mina_lang.common;

import java.util.List;

import org.eclipse.collections.api.list.ImmutableList;

public interface DiagnosticCollector {
    public List<Diagnostic> getDiagnostics();

    public void reportError(Range range, String message);

    public void reportError(Range range, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation);

    public void reportWarning(Range range, String message);

    public void reportWarning(Range range, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation);

    public void reportInfo(Range range, String message);

    public void reportInfo(Range range, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation);

    public void reportHint(Range range, String message);

    public void reportHint(Range range, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation);
}

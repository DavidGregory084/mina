package org.mina_lang.common;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public record Diagnostic(Range range, DiagnosticSeverity severity, String message,
        ImmutableList<DiagnosticRelatedInformation> relatedInformation) {

    public Diagnostic(Range range, DiagnosticSeverity severity, String message) {
        this(range, severity, message, Lists.immutable.empty());
    }
}

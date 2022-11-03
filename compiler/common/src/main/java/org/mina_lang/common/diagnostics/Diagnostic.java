package org.mina_lang.common.diagnostics;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Range;

public record Diagnostic(Range range, DiagnosticSeverity severity, String message,
        ImmutableList<DiagnosticRelatedInformation> relatedInformation) {

    public Diagnostic(Range range, DiagnosticSeverity severity, String message) {
        this(range, severity, message, Lists.immutable.empty());
    }
}

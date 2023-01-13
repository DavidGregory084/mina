package org.mina_lang.common.diagnostics;

import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Location;

public record Diagnostic(Location location, DiagnosticSeverity severity, String message,
        ImmutableList<DiagnosticRelatedInformation> relatedInformation) {

    public Diagnostic(Location location, DiagnosticSeverity severity, String message) {
        this(location, severity, message, Lists.immutable.empty());
    }
}

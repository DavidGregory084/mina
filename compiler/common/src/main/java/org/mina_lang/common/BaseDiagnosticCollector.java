package org.mina_lang.common;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

public class BaseDiagnosticCollector implements DiagnosticCollector {
    MutableList<Diagnostic> diagnostics = Lists.mutable.empty();

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    @Override
    public void reportError(Range range, String message) {
        diagnostics.add(new Diagnostic(range, DiagnosticSeverity.Error, message));
    }

    @Override
    public void reportWarning(Range range, String message) {
        diagnostics.add(new Diagnostic(range, DiagnosticSeverity.Warning, message));
    }

    @Override
    public void reportInfo(Range range, String message) {
        diagnostics.add(new Diagnostic(range, DiagnosticSeverity.Information, message));
    }

    @Override
    public void reportHint(Range range, String message) {
        diagnostics.add(new Diagnostic(range, DiagnosticSeverity.Hint, message));
    }
}

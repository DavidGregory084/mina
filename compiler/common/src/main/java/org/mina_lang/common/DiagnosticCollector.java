package org.mina_lang.common;

import java.util.List;

public interface DiagnosticCollector {
    public List<Diagnostic> getDiagnostics();

    public void reportError(Range range, String message);

    public void reportWarning(Range range, String message);

    public void reportInfo(Range range, String message);

    public void reportHint(Range range, String message);
}

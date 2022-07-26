package org.mina_lang.common;

public record Diagnostic(Range range, DiagnosticSeverity severity, String message) {

}

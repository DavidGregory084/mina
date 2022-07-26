package org.mina_lang.common;

public enum DiagnosticSeverity {
    Error(1),
    Warning(2),
    Information(3),
    Hint(4);

    private int code;

    DiagnosticSeverity(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

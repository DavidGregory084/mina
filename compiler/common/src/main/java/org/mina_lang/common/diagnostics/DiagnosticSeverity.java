/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.diagnostics;

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

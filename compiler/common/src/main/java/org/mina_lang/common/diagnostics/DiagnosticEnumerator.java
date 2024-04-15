/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.diagnostics;

public interface DiagnosticEnumerator {
    int errorCount();

    int warningCount();

    boolean hasErrors();

    boolean hasWarnings();
}

/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.diagnostics;

import java.util.List;

public interface DiagnosticCollector {
    List<Diagnostic> getDiagnostics();
}

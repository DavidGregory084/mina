/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver;

import org.eclipse.lsp4j.*;
import org.mina_lang.common.diagnostics.DiagnosticCollector;

import java.util.List;
import java.util.stream.Collectors;

public interface LSPDiagnosticCollector extends DiagnosticCollector {
    default List<Diagnostic> getLSPDiagnostics() {
        return getDiagnostics().stream()
                .distinct()
                .map(Conversions::toLspDiagnostic)
                .toList();
    }
}

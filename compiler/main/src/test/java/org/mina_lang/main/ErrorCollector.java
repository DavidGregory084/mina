/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.main;

import org.mina_lang.common.diagnostics.BaseDiagnosticCollector;

import java.util.List;
import java.util.stream.Collectors;

public class ErrorCollector extends BaseDiagnosticCollector {
    public List<String> getErrors() {
        return getDiagnostics().stream()
            .map(diagnostic -> diagnostic.message())
            .collect(Collectors.toList());
    }
}

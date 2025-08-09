/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.diagnostics;

import org.mina_lang.common.Location;

import java.util.List;

public interface DiagnosticReporter {
    void reportError(Location location, String message);

    void reportError(Location location, String message, List<DiagnosticRelatedInformation> relatedInformation);

    void reportWarning(Location location, String message);

    void reportWarning(Location location, String message, List<DiagnosticRelatedInformation> relatedInformation);

    void reportInfo(Location location, String message);

    void reportInfo(Location location, String message, List<DiagnosticRelatedInformation> relatedInformation);

    void reportHint(Location location, String message);

    void reportHint(Location location, String message, List<DiagnosticRelatedInformation> relatedInformation);
}

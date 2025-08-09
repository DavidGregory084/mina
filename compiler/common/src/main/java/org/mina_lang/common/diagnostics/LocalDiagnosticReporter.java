/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.diagnostics;

import com.opencastsoftware.yvette.Range;
import org.mina_lang.common.Location;

import java.net.URI;
import java.util.List;

public interface LocalDiagnosticReporter extends DiagnosticReporter {

    URI getSourceUri();

    default void reportError(Range range, String message) {
        reportError(new Location(getSourceUri(), range), message);
    }

    default void reportError(Range range, String message, List<DiagnosticRelatedInformation> relatedInformation) {
        reportError(new Location(getSourceUri(), range), message, relatedInformation);
    }

    default void reportWarning(Range range, String message) {
        reportWarning(new Location(getSourceUri(), range), message);
    }

    default void reportWarning(Range range, String message, List<DiagnosticRelatedInformation> relatedInformation) {
        reportWarning(new Location(getSourceUri(), range), message, relatedInformation);
    }

    default void reportInfo(Range range, String message) {
        reportInfo(new Location(getSourceUri(), range), message);
    }

    default void reportInfo(Range range, String message, List<DiagnosticRelatedInformation> relatedInformation) {
        reportInfo(new Location(getSourceUri(), range), message, relatedInformation);
    }

    default void reportHint(Range range, String message) {
        reportInfo(new Location(getSourceUri(), range), message);
    }

    default void reportHint(Range range, String message, List<DiagnosticRelatedInformation> relatedInformation) {
        reportInfo(new Location(getSourceUri(), range), message, relatedInformation);
    }
}

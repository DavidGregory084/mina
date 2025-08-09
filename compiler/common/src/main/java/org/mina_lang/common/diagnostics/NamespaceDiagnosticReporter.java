/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.diagnostics;

import org.mina_lang.common.Location;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NamespaceDiagnosticReporter implements LocalDiagnosticReporter, DiagnosticEnumerator {
    AtomicInteger errorCount = new AtomicInteger(0);
    AtomicInteger warningCount = new AtomicInteger(0);

    private DiagnosticReporter recipient;
    private URI sourceUri;

    public NamespaceDiagnosticReporter(DiagnosticReporter recipient, URI sourceUri) {
        this.recipient = recipient;
        this.sourceUri = sourceUri;
    }

    @Override
    public void reportError(Location location, String message) {
        errorCount.incrementAndGet();
        recipient.reportError(location, message);
    }

    @Override
    public void reportError(Location location, String message,
            List<DiagnosticRelatedInformation> relatedInformation) {
        errorCount.incrementAndGet();
        recipient.reportError(location, message, relatedInformation);
    }

    @Override
    public void reportWarning(Location location, String message) {
        warningCount.incrementAndGet();
        recipient.reportWarning(location, message);
    }

    @Override
    public void reportWarning(Location location, String message,
            List<DiagnosticRelatedInformation> relatedInformation) {
        warningCount.incrementAndGet();
        recipient.reportWarning(location, message, relatedInformation);
    }

    @Override
    public void reportInfo(Location location, String message) {
        recipient.reportInfo(location, message);
    }

    @Override
    public void reportInfo(Location location, String message,
            List<DiagnosticRelatedInformation> relatedInformation) {
        recipient.reportInfo(location, message, relatedInformation);
    }

    @Override
    public void reportHint(Location location, String message) {
        recipient.reportHint(location, message);
    }

    @Override
    public void reportHint(Location location, String message,
            List<DiagnosticRelatedInformation> relatedInformation) {
        recipient.reportHint(location, message, relatedInformation);
    }

    @Override
    public URI getSourceUri() {
        return sourceUri;
    }

    @Override
    public int errorCount() {
        return errorCount.get();
    }

    @Override
    public int warningCount() {
        return warningCount.get();
    }

    @Override
    public boolean hasErrors() {
        return errorCount() > 0;
    }

    @Override
    public boolean hasWarnings() {
        return warningCount() > 0;
    }
}

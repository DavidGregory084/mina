/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.diagnostics;

import com.opencastsoftware.yvette.Severity;
import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.*;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseDiagnosticCollector implements DiagnosticCollector {
    ConcurrentLinkedQueue<Diagnostic> diagnostics = new ConcurrentLinkedQueue<>();
    AtomicInteger errorCount = new AtomicInteger(0);
    AtomicInteger warningCount = new AtomicInteger(0);

    public List<Diagnostic> getDiagnostics() {
        return diagnostics.stream().toList();
    }

    @Override
    public void reportError(Location location, String message) {
        diagnostics.offer(new Diagnostic(location, Severity.Error, message));
        errorCount.incrementAndGet();
    }

    @Override
    public void reportError(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        diagnostics.offer(new Diagnostic(location, Severity.Error, message, relatedInformation));
        errorCount.incrementAndGet();
    }

    @Override
    public void reportWarning(Location location, String message) {
        diagnostics.offer(new Diagnostic(location, Severity.Warning, message));
        warningCount.incrementAndGet();
    }

    @Override
    public void reportWarning(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        diagnostics.offer(new Diagnostic(location, Severity.Warning, message, relatedInformation));
        warningCount.incrementAndGet();
    }

    @Override
    public void reportInfo(Location location, String message) {
        diagnostics.offer(new Diagnostic(location, Severity.Information, message));
    }

    @Override
    public void reportInfo(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        diagnostics.offer(new Diagnostic(location, Severity.Information, message, relatedInformation));
    }

    @Override
    public void reportHint(Location location, String message) {
        diagnostics.offer(new Diagnostic(location, Severity.Hint, message));
    }

    @Override
    public void reportHint(Location location, String message,
            ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        diagnostics.offer(new Diagnostic(location, Severity.Hint, message, relatedInformation));
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

/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.diagnostics;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Location;

import java.util.List;

public interface DiagnosticCollector {
    public List<Diagnostic> getDiagnostics();

    public int errorCount();

    public int warningCount();

    public boolean hasErrors();

    public boolean hasWarnings();

    public void reportError(Location location, String message);

    public void reportError(Location location, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation);

    public void reportWarning(Location location, String message);

    public void reportWarning(Location location, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation);

    public void reportInfo(Location location, String message);

    public void reportInfo(Location location, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation);

    public void reportHint(Location location, String message);

    public void reportHint(Location location, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation);
}

/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle.diagnostics;

import org.eclipse.collections.api.list.ImmutableList;
import org.gradle.api.problems.ProblemReporter;
import org.gradle.api.problems.Problems;
import org.gradle.api.problems.Severity;
import org.mina_lang.common.Location;
import org.mina_lang.common.diagnostics.BaseDiagnosticCollector;
import org.mina_lang.common.diagnostics.DiagnosticRelatedInformation;
import org.mina_lang.gradle.BuildInfo;

import java.nio.file.Paths;

public class MinaProblemReporter extends BaseDiagnosticCollector {
    private ProblemReporter reporter;

    public MinaProblemReporter(Problems problems) {
        this.reporter = problems.forNamespace(BuildInfo.pluginId);
    }

    @Override
    public void reportError(Location location, String message) {
        super.reportError(location, message);
        reportProblem(Severity.ERROR, location, message);
    }

    @Override
    public void reportError(Location location, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        // TODO: handle related info
        reportError(location, message);
    }

    @Override
    public void reportWarning(Location location, String message) {
        super.reportWarning(location, message);
        reportProblem(Severity.WARNING, location, message);
    }

    @Override
    public void reportWarning(Location location, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        // TODO: handle related info
        reportWarning(location, message);
    }

    @Override
    public void reportInfo(Location location, String message) {
        super.reportInfo(location, message);
        reportProblem(Severity.ADVICE, location, message);
    }

    @Override
    public void reportInfo(Location location, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        // TODO: handle related info
        reportInfo(location, message);
    }

    @Override
    public void reportHint(Location location, String message) {
        super.reportHint(location, message);
        reportProblem(Severity.ADVICE, location, message);
    }

    @Override
    public void reportHint(Location location, String message, ImmutableList<DiagnosticRelatedInformation> relatedInformation) {
        // TODO: handle related info
        reportHint(location, message);
    }

    void reportProblem(Severity severity, Location location, String message) {
        reporter.reporting(spec -> {
            var startLocation = location.range().start();
            spec.category("compilation", "mina")
                .label(message)
                .severity(severity)
                .lineInFileLocation(
                    Paths.get(location.uri()).toString(),
                    startLocation.line() + 1,
                    startLocation.character() + 1);
        });
    }
}

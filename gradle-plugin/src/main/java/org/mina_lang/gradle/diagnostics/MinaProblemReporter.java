/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle.diagnostics;

import com.opencastsoftware.yvette.handlers.ReportHandler;
import com.opencastsoftware.yvette.handlers.graphical.GraphicalReportHandler;
import org.gradle.api.problems.*;
import org.gradle.internal.UncheckedException;
import org.mina_lang.common.diagnostics.BaseDiagnosticCollector;
import org.mina_lang.common.diagnostics.Diagnostic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MinaProblemReporter extends BaseDiagnosticCollector {
    private static Logger logger = LoggerFactory.getLogger(MinaProblemReporter.class);
    private static ProblemGroup COMPILATION = ProblemGroup.create("compilation", "Compilation");
    private static ProblemId MINA = ProblemId.create("mina", "Mina Compilation", COMPILATION);
    private ProblemReporter reporter;
    private ReportHandler reportHandler;
    private StringBuilder stringBuilder;

    public MinaProblemReporter(Problems problems) {
        this.reporter = problems.getReporter();
        this.stringBuilder = new StringBuilder();
        this.reportHandler = GraphicalReportHandler.builder()
            .withColours(true)
            .withUnicode(false)
            .buildFor(stringBuilder);
    }

    public void reportProblems() {
        Diagnostic diagnostic;
        while ((diagnostic = diagnostics.poll()) != null) {
            // Report diagnostic via the Problems API
            var location = diagnostic.location();
            var message = diagnostic.message();
            var startLocation = location.range().start();
            var severity = switch (diagnostic.severity()) {
                case Error -> Severity.ERROR;
                case Warning -> Severity.WARNING;
                case Information, Hint -> Severity.ADVICE;
            };

            reporter.report(MINA, spec -> {
                spec.severity(severity)
                    .contextualLabel(message)
                    .lineInFileLocation(
                        location.uri().toString(),
                        startLocation.line() + 1,
                        startLocation.character() + 1);
            });

            // Report diagnostic to the Gradle CLI log
            stringBuilder.setLength(0);

            try {
                reportHandler.display(diagnostic, stringBuilder);

                switch (diagnostic.severity()) {
                    case Error -> logger.error(stringBuilder.toString());
                    case Warning -> logger.warn(stringBuilder.toString());
                    case Information, Hint -> logger.info(stringBuilder.toString());
                }
            } catch (IOException e) {
                UncheckedException.throwAsUncheckedException(e);
            }
        }
    }
}

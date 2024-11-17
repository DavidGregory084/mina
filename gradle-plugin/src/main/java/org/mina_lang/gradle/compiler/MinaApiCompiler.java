/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle.compiler;

import com.opencastsoftware.yvette.handlers.ReportHandler;
import com.opencastsoftware.yvette.handlers.graphical.GraphicalReportHandler;
import org.apache.commons.lang3.function.Failable;
import org.gradle.api.problems.Problems;
import org.gradle.internal.UncheckedException;
import org.mina_lang.gradle.MinaCompilationException;
import org.mina_lang.gradle.MinaCompileParameters;
import org.mina_lang.gradle.diagnostics.MinaProblemReporter;
import org.mina_lang.main.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

public class MinaApiCompiler implements MinaCompiler {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Problems problems;
    private ReportHandler reportHandler;
    private StringBuilder stringBuilder;

    public MinaApiCompiler(Problems problems) {
        this.problems = problems;
        this.stringBuilder = new StringBuilder();
        this.reportHandler = GraphicalReportHandler.builder()
            .withColours(true)
            .withUnicode(false)
            .buildFor(stringBuilder);
    }

    @Override
    public void compile(MinaCompileParameters compileParameters) throws IOException {
        var problemReporter = new MinaProblemReporter(problems);
        var compiler = new Main(problemReporter);

        var classpath = Failable.stream(compileParameters.getClasspath().getFiles())
            .map(file -> file.toURI().toURL())
            .stream().toArray(URL[]::new);

        var destDir = compileParameters
            .getDestinationDirectory().get()
            .getAsFile().toPath();

        var sourcePaths = compileParameters
            .getSourceFiles().getFiles()
            .stream()
            .map(File::toPath)
            .toArray(Path[]::new);

        try {
            compiler.compileSourcePaths(classpath, destDir, sourcePaths).join();

            // Report diagnostics to the Gradle CLI log
            problemReporter.getDiagnostics().forEach(diagnostic -> {
                // Clear the StringBuilder
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
            });

            if (problemReporter.hasErrors()) {
                throw new MinaCompilationException("Compilation failed. See above for more details.");
            }
        } catch (Exception e) {
            UncheckedException.throwAsUncheckedException(e);
        }
    }
}

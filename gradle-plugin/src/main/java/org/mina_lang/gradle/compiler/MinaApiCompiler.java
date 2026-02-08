/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle.compiler;

import org.apache.commons.lang3.function.Failable;
import org.gradle.api.problems.Problems;
import org.gradle.internal.UncheckedException;
import org.mina_lang.gradle.MinaCompilationException;
import org.mina_lang.gradle.MinaCompileParameters;
import org.mina_lang.gradle.diagnostics.MinaProblemReporter;
import org.mina_lang.main.Main;
import org.mina_lang.reporting.HtmlReportGenerator;
import org.mina_lang.reporting.NoOpReportGenerator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

public class MinaApiCompiler implements MinaCompiler {

    private Problems problems;

    public MinaApiCompiler(Problems problems) {
        this.problems = problems;
    }

    @Override
    public void compile(MinaCompileParameters compileParameters) throws IOException {
        var problemReporter = new MinaProblemReporter(problems);

        var reportDir = compileParameters
            .getReportDirectory().get()
            .getAsFile().toPath();

        var compiler = compileParameters.getReportingEnabled().get()
            ? new Main(problemReporter, new HtmlReportGenerator(reportDir))
            : new Main(problemReporter, NoOpReportGenerator.INSTANCE);

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
            var compilation = compiler.compileSourcePaths(classpath, destDir, sourcePaths);

            while (!compilation.isDone()) {
                problemReporter.reportProblems();
            }

            problemReporter.reportProblems();

            problemReporter.getDiagnostics().forEach(diagnostic -> {
            });

            if (problemReporter.hasErrors()) {
                throw new MinaCompilationException("Compilation failed. See above for more details.");
            }
        } catch (Exception e) {
            UncheckedException.throwAsUncheckedException(e);
        }
    }
}

/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle.compiler;

import org.gradle.api.problems.Problems;
import org.gradle.internal.UncheckedException;
import org.mina_lang.gradle.MinaCompilationException;
import org.mina_lang.gradle.MinaCompileParameters;
import org.mina_lang.gradle.diagnostics.MinaProblemReporter;
import org.mina_lang.main.Main;

import java.io.File;
import java.nio.file.Path;

public class MinaApiCompiler implements MinaCompiler {
    private Problems problems;

    public MinaApiCompiler(Problems problems) {
        this.problems = problems;
    }

    @Override
    public void compile(MinaCompileParameters compileParameters) {
        var problemReporter = new MinaProblemReporter(problems);
        var compiler = new Main(problemReporter);

        var destDir = compileParameters
            .getDestinationDirectory().get()
            .getAsFile().toPath();

        var sourcePaths = compileParameters
            .getSourceFiles().getFiles()
            .stream()
            .map(File::toPath)
            .toArray(Path[]::new);

        try {
            compiler.compileSourcePaths(destDir, sourcePaths).join();

            if (problemReporter.hasErrors()) {
                throw new MinaCompilationException("Compilation failed. See log for more details");
            }
        } catch (Exception e) {
            UncheckedException.throwAsUncheckedException(e);
        }
    }
}

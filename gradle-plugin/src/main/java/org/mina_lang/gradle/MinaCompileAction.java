/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle;

import org.gradle.api.problems.Problems;
import org.gradle.workers.WorkAction;
import org.mina_lang.gradle.compiler.MinaApiCompiler;

import javax.inject.Inject;

public abstract class MinaCompileAction implements WorkAction<MinaCompileParameters> {
    private Problems problems;

    @Inject
    public MinaCompileAction(Problems problems) {
        this.problems = problems;
    }

    @Override
    public void execute() {
        var compiler = new MinaApiCompiler(problems);
        compiler.compile(getParameters());
    }
}

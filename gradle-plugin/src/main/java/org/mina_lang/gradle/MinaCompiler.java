package org.mina_lang.gradle;

import org.gradle.api.tasks.WorkResult;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.workers.internal.DefaultWorkResult;

public class MinaCompiler implements Compiler<DefaultMinaCompileSpec> {
    @Override
    public WorkResult execute(DefaultMinaCompileSpec compileSpec) {
        return DefaultWorkResult.SUCCESS;
    }
}

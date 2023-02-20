package org.mina_lang.gradle;

import java.io.Serializable;

import org.gradle.api.tasks.WorkResult;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.workers.internal.DefaultWorkResult;

public class MinaCompiler implements Compiler<DefaultMinaCompileSpec>, Serializable {
    @Override
    public WorkResult execute(DefaultMinaCompileSpec compileSpec) {
        return DefaultWorkResult.SUCCESS;
    }
}

package org.mina_lang.gradle;

import java.io.File;

import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.language.base.internal.compile.CompilerFactory;
import org.gradle.process.internal.JavaForkOptionsFactory;
import org.gradle.workers.internal.ActionExecutionSpecFactory;
import org.gradle.workers.internal.WorkerFactory;

public class MinaCompilerFactory implements CompilerFactory<DefaultMinaCompileSpec> {

    private File daemonWorkingDir;
    private JavaForkOptionsFactory forkOptionsFactory;
    private WorkerFactory workerFactory;
    private ActionExecutionSpecFactory actionExecutionSpecFactory;

    public MinaCompilerFactory(File daemonWorkingDir, JavaForkOptionsFactory forkOptionsFactory,
            WorkerFactory workerFactory, ActionExecutionSpecFactory actionExecutionSpecFactory) {
        this.daemonWorkingDir = daemonWorkingDir;
        this.forkOptionsFactory = forkOptionsFactory;
        this.workerFactory = workerFactory;
        this.actionExecutionSpecFactory = actionExecutionSpecFactory;
    }

    @Override
    public Compiler<DefaultMinaCompileSpec> newCompiler(DefaultMinaCompileSpec compileSpec) {
        return new DaemonMinaCompiler<DefaultMinaCompileSpec>(daemonWorkingDir, MinaCompiler.class, new Object[] {},
                forkOptionsFactory, workerFactory, actionExecutionSpecFactory);
    }

}

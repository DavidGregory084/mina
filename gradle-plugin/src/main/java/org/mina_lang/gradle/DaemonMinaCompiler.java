package org.mina_lang.gradle;

import java.io.File;

import org.gradle.api.internal.tasks.compile.BaseForkOptionsConverter;
import org.gradle.api.internal.tasks.compile.daemon.AbstractDaemonCompiler;
import org.gradle.language.base.internal.compile.CompileSpec;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.process.JavaForkOptions;
import org.gradle.process.internal.JavaForkOptionsFactory;
import org.gradle.workers.internal.ActionExecutionSpecFactory;
import org.gradle.workers.internal.DaemonForkOptions;
import org.gradle.workers.internal.DaemonForkOptionsBuilder;
import org.gradle.workers.internal.KeepAliveMode;
import org.gradle.workers.internal.WorkerFactory;

public class DaemonMinaCompiler<T extends MinaCompileSpec> extends AbstractDaemonCompiler<T> {
    private File daemonWorkingDir;
    private final Class<? extends Compiler<T>> compilerClass;
    private Object[] compilerConstructorArguments;
    private JavaForkOptionsFactory forkOptionsFactory;

    public DaemonMinaCompiler(File daemonWorkingDir, Class<? extends Compiler<T>> compilerClass,
            Object[] compilerConstructorArguments,
            JavaForkOptionsFactory forkOptionsFactory,
            WorkerFactory workerFactory, ActionExecutionSpecFactory actionExecutionSpecFactory) {
        super(workerFactory, actionExecutionSpecFactory);
        this.daemonWorkingDir = daemonWorkingDir;
        this.compilerClass = compilerClass;
        this.compilerConstructorArguments = compilerConstructorArguments;
        this.forkOptionsFactory = forkOptionsFactory;
    }

    @Override
    protected CompilerParameters getCompilerParameters(T compileSpec) {
        return new MinaCompilerParameters<MinaCompileSpec>(compilerClass.getName(), compilerConstructorArguments,
                compileSpec);
    }

    @Override
    protected DaemonForkOptions toDaemonForkOptions(T compileSpec) {
        MinimalMinaCompilerDaemonForkOptions minaOptions = compileSpec.getMinaCompileOptions().getForkOptions();
        JavaForkOptions javaForkOptions = new BaseForkOptionsConverter(forkOptionsFactory).transform(minaOptions);
        javaForkOptions.setWorkingDir(daemonWorkingDir);

        return new DaemonForkOptionsBuilder(forkOptionsFactory)
                .javaForkOptions(javaForkOptions)
                .keepAliveMode(KeepAliveMode.SESSION)
                .build();
    }

    public static class MinaCompilerParameters<T extends MinaCompileSpec> extends CompilerParameters {
        private T compileSpec;

        public MinaCompilerParameters(String compilerClassName, Object[] compilerInstanceParameters, T compileSpec) {
            super(compilerClassName, compilerInstanceParameters);
            this.compileSpec = compileSpec;
        }

        @Override
        public CompileSpec getCompileSpec() {
            return compileSpec;
        }
    }
}

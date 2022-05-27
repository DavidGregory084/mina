package org.mina_lang.gradle;

import javax.inject.Inject;

import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Classpath;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.process.internal.JavaForkOptionsFactory;
import org.gradle.workers.internal.ActionExecutionSpecFactory;
import org.gradle.workers.internal.WorkerDaemonFactory;
import org.gradle.process.internal.worker.child.WorkerDirectoryProvider;

public class MinaCompile extends AbstractMinaCompile {

    private FileCollection minaClasspath;

    private Compiler<DefaultMinaCompileSpec> compiler;

    @Inject
    public MinaCompile(ObjectFactory objectFactory) {
        super(new MinaCompileOptions(), objectFactory);
    }

    public MinaCompileOptions getMinaCompileOptions() {
        return (MinaCompileOptions) super.getMinaCompileOptions();
    }

    @Classpath
    public FileCollection getMinaClasspath() {
        return minaClasspath;
    }

    public void setMinaClasspath(FileCollection minaClasspath) {
        this.minaClasspath = minaClasspath;
    }

    public void setCompiler(Compiler<DefaultMinaCompileSpec> compiler) {
        this.compiler = compiler;
    }

    @Override
    protected Compiler<DefaultMinaCompileSpec> getCompiler(DefaultMinaCompileSpec spec) {
        if (compiler == null) {
            WorkerDaemonFactory workerDaemonFactory = getServices().get(WorkerDaemonFactory.class);
            JavaForkOptionsFactory forkOptionsFactory = getServices().get(JavaForkOptionsFactory.class);
            ActionExecutionSpecFactory actionExecutionSpecFactory = getServices().get(ActionExecutionSpecFactory.class);
            MinaCompilerFactory minaCompilerFactory = new MinaCompilerFactory(
                    getServices().get(WorkerDirectoryProvider.class).getWorkingDirectory(),
                    forkOptionsFactory, workerDaemonFactory, actionExecutionSpecFactory);
            compiler = minaCompilerFactory.newCompiler(spec);
        }

        return compiler;
    }

}

package org.mina_lang.gradle;

import java.io.File;

import javax.inject.Inject;

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.ClassPathRegistry;
import org.gradle.api.internal.tasks.compile.daemon.ClassloaderIsolatedCompilerWorkerExecutor;
import org.gradle.api.internal.tasks.compile.daemon.CompilerWorkerExecutor;
import org.gradle.api.internal.tasks.compile.daemon.ProcessIsolatedCompilerWorkerExecutor;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Nested;
import org.gradle.initialization.ClassLoaderRegistry;
import org.gradle.internal.classloader.ClasspathHasher;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.process.internal.JavaForkOptionsFactory;
import org.gradle.process.internal.worker.child.WorkerDirectoryProvider;
import org.gradle.workers.internal.ActionExecutionSpecFactory;
import org.gradle.workers.internal.IsolatedClassloaderWorkerFactory;
import org.gradle.workers.internal.WorkerDaemonFactory;

@CacheableTask
public abstract class MinaCompile extends AbstractMinaCompile {

    private FileCollection minaClasspath;

    private Compiler<DefaultMinaCompileSpec> compiler;

    @Inject
    public MinaCompile() {
    }

    @Nested
    @Override
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
        assertMinaClasspathIsNonEmpty();
        if (compiler == null) {
            WorkerDaemonFactory workerDaemonFactory = getServices().get(WorkerDaemonFactory.class);
            IsolatedClassloaderWorkerFactory inProcessWorkerFactory = getServices()
                    .get(IsolatedClassloaderWorkerFactory.class);
            JavaForkOptionsFactory forkOptionsFactory = getServices().get(JavaForkOptionsFactory.class);
            ClassPathRegistry classPathRegistry = getServices().get(ClassPathRegistry.class);
            ClassLoaderRegistry classLoaderRegistry = getServices().get(ClassLoaderRegistry.class);
            ActionExecutionSpecFactory actionExecutionSpecFactory = getServices().get(ActionExecutionSpecFactory.class);
            File daemonWorkingDir = getServices().get(WorkerDirectoryProvider.class).getWorkingDirectory();
            CompilerWorkerExecutor compilerWorkerExecutor = getMinaCompileOptions().getIsFork().get()
                    ? new ProcessIsolatedCompilerWorkerExecutor(workerDaemonFactory, actionExecutionSpecFactory)
                    : new ClassloaderIsolatedCompilerWorkerExecutor(inProcessWorkerFactory, actionExecutionSpecFactory);
            ClasspathHasher classpathHasher = getServices().get(ClasspathHasher.class);
            MinaCompilerFactory minaCompilerFactory = new MinaCompilerFactory(
                    daemonWorkingDir,
                    compilerWorkerExecutor,
                    getMinaClasspath(),
                    forkOptionsFactory,
                    classPathRegistry,
                    classLoaderRegistry,
                    classpathHasher);
            compiler = minaCompilerFactory.newCompiler(spec);
        }

        return compiler;
    }

    protected void assertMinaClasspathIsNonEmpty() {
        if (getMinaClasspath().isEmpty()) {
            throw new InvalidUserDataException("'" + getName()
                    + ".minaClasspath' must not be empty. If a Mina compile dependency is provided, "
                    + "the 'mina-base' plugin will attempt to configure 'minaClasspath' automatically. Alternatively, you may configure 'minaClasspath' explicitly.");
        }
    }

}

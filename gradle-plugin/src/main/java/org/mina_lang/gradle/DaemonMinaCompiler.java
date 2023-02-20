package org.mina_lang.gradle;

import java.io.File;

import org.gradle.api.internal.ClassPathRegistry;
import org.gradle.api.internal.tasks.compile.BaseForkOptionsConverter;
import org.gradle.api.internal.tasks.compile.daemon.AbstractDaemonCompiler;
import org.gradle.api.internal.tasks.compile.daemon.CompilerWorkerExecutor;
import org.gradle.initialization.ClassLoaderRegistry;
import org.gradle.internal.classloader.FilteringClassLoader;
import org.gradle.internal.classloader.VisitableURLClassLoader;
import org.gradle.internal.classpath.ClassPath;
import org.gradle.internal.classpath.DefaultClassPath;
import org.gradle.language.base.internal.compile.CompileSpec;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.process.JavaForkOptions;
import org.gradle.process.internal.JavaForkOptionsFactory;
import org.gradle.workers.internal.DaemonForkOptions;
import org.gradle.workers.internal.DaemonForkOptionsBuilder;
import org.gradle.workers.internal.HierarchicalClassLoaderStructure;

public class DaemonMinaCompiler<T extends DefaultMinaCompileSpec> extends AbstractDaemonCompiler<T> {
    private final File daemonWorkingDir;
    private final Class<? extends Compiler<T>> compilerClass;
    private final Object[] compilerConstructorArguments;
    private final JavaForkOptionsFactory forkOptionsFactory;
    private final ClassPathRegistry classPathRegistry;
    private final ClassLoaderRegistry classLoaderRegistry;

    public DaemonMinaCompiler(
            File daemonWorkingDir,
            Class<? extends Compiler<T>> compilerClass,
            Object[] compilerConstructorArguments,
            CompilerWorkerExecutor compilerWorkerExecutor,
            JavaForkOptionsFactory forkOptionsFactory,
            ClassPathRegistry classPathRegistry,
            ClassLoaderRegistry classLoaderRegistry) {
        super(compilerWorkerExecutor);
        this.daemonWorkingDir = daemonWorkingDir;
        this.compilerClass = compilerClass;
        this.compilerConstructorArguments = compilerConstructorArguments;
        this.forkOptionsFactory = forkOptionsFactory;
        this.classPathRegistry = classPathRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Override
    protected CompilerWorkerExecutor.CompilerParameters getCompilerParameters(T compileSpec) {
        return new MinaCompilerParameters<DefaultMinaCompileSpec>(
                compilerClass.getName(),
                compilerConstructorArguments,
                compileSpec);
    }

    @Override
    protected DaemonForkOptions toDaemonForkOptions(T compileSpec) {
        MinimalMinaCompileOptions compileOptions = compileSpec.getMinaCompileOptions();
        MinimalMinaCompilerDaemonForkOptions forkOptions = compileOptions.getForkOptions();
        JavaForkOptions javaForkOptions = new BaseForkOptionsConverter(forkOptionsFactory).transform(forkOptions);
        javaForkOptions.setWorkingDir(daemonWorkingDir);
        javaForkOptions.setExecutable(compileSpec.getJavaExecutable());

        ClassPath compilerClasspath = DefaultClassPath.of(compileSpec.getMinaClasspath());

        HierarchicalClassLoaderStructure classLoaderStructure = new HierarchicalClassLoaderStructure(
                classLoaderRegistry.getGradleWorkerExtensionSpec())
                .withChild(getMinaFilterSpec())
                .withChild(new VisitableURLClassLoader.Spec("compiler", compilerClasspath.getAsURLs()));

        return new DaemonForkOptionsBuilder(forkOptionsFactory)
                .javaForkOptions(javaForkOptions)
                .withClassLoaderStructure(classLoaderStructure)
                .keepAliveMode(compileOptions.getKeepAliveMode())
                .build();
    }

    private FilteringClassLoader.Spec getMinaFilterSpec() {
        FilteringClassLoader.Spec gradleApiAndMinaSpec = classLoaderRegistry.getGradleApiFilterSpec();
        gradleApiAndMinaSpec.allowClass(MinaCompilerParameters.class);
        return gradleApiAndMinaSpec;
    }

    public static class MinaCompilerParameters<T extends DefaultMinaCompileSpec>
            extends CompilerWorkerExecutor.CompilerParameters {
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

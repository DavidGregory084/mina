package org.mina_lang.gradle;

import java.io.File;
import java.util.Set;

import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.ClassPathRegistry;
import org.gradle.api.internal.tasks.compile.daemon.CompilerWorkerExecutor;
import org.gradle.api.internal.tasks.scala.HashedClasspath;
import org.gradle.initialization.ClassLoaderRegistry;
import org.gradle.internal.classloader.ClasspathHasher;
import org.gradle.internal.classpath.DefaultClassPath;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.language.base.internal.compile.CompilerFactory;
import org.gradle.process.internal.JavaForkOptionsFactory;

public class MinaCompilerFactory implements CompilerFactory<DefaultMinaCompileSpec> {
    private final CompilerWorkerExecutor compilerWorkerExecutor;
    private final FileCollection minaClasspath;
    private final File daemonWorkingDir;
    private final JavaForkOptionsFactory forkOptionsFactory;
    private final ClassPathRegistry classPathRegistry;
    private final ClassLoaderRegistry classLoaderRegistry;
    private final ClasspathHasher classpathHasher;

    public MinaCompilerFactory(
        File daemonWorkingDir,
        CompilerWorkerExecutor compilerWorkerExecutor,
        FileCollection minaClasspath,
        JavaForkOptionsFactory forkOptionsFactory,
        ClassPathRegistry classPathRegistry,
        ClassLoaderRegistry classLoaderRegistry,
        ClasspathHasher classpathHasher) {
        this.daemonWorkingDir = daemonWorkingDir;
        this.compilerWorkerExecutor = compilerWorkerExecutor;
        this.minaClasspath = minaClasspath;
        this.forkOptionsFactory = forkOptionsFactory;
        this.classPathRegistry = classPathRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
        this.classpathHasher = classpathHasher;
    }

    @Override
    public Compiler<DefaultMinaCompileSpec> newCompiler(DefaultMinaCompileSpec compileSpec) {
        // Set<File> minaClasspathFiles = minaClasspath.getFiles();

        // HashedClasspath hashedMinaClasspath = new HashedClasspath(DefaultClassPath.of(minaClasspathFiles), classpathHasher);

        return new DaemonMinaCompiler<DefaultMinaCompileSpec>(
            daemonWorkingDir,
            MinaCompiler.class, new Object[] {},
            compilerWorkerExecutor,
            forkOptionsFactory,
            classPathRegistry,
            classLoaderRegistry);
    }

}

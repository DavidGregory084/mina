package org.mina_lang.gradle;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency;
import org.gradle.api.internal.tasks.compile.HasCompileOptions;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.jvm.internal.JvmEcosystemUtilities;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.ForkOptions;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

@CacheableTask
public abstract class MinaCompile extends AbstractCompile implements HasCompileOptions {

    private CompileOptions compileOptions;
    private ConfigurableFileCollection minaCompilerClasspath;

    @Inject
    public MinaCompile(Project project, JvmEcosystemUtilities jvmEcosystemUtilities) {
        this.compileOptions = getObjectFactory().newInstance(CompileOptions.class);

        Property<String> minaVersion = project.getExtensions().getByType(MinaExtension.class).getMinaVersion();

        this.minaCompilerClasspath = getObjectFactory().fileCollection().from(
                minaVersion.map(version -> {
                    Configuration classpath = project.getConfigurations().detachedConfiguration(
                            new DefaultExternalModuleDependency("org.mina-lang", "mina-compiler", version));
                    jvmEcosystemUtilities.configureAsRuntimeClasspath(classpath);
                    return classpath.getAsFileTree();
                }));
    }

    @Nested
    public abstract MinaCompileOptions getMinaCompileOptions();

    @Classpath
    public FileCollection getMinaCompilerClasspath() {
        return minaCompilerClasspath;
    }

    @Nested
    public CompileOptions getOptions() {
        return compileOptions;
    }

    @Nested
    public abstract Property<JavaLauncher> getJavaLauncher();

    @Inject
    public abstract ObjectFactory getObjectFactory();

    @Inject
    public abstract WorkerExecutor getWorkerExecutor();

    @TaskAction
    public void compile() {
        WorkQueue workQueue = getOptions().isFork() ? getWorkerExecutor().processIsolation(spec -> {
            spec.getClasspath().from(getMinaCompilerClasspath());
            spec.forkOptions(opts -> {
                ForkOptions forkOpts = getOptions().getForkOptions();
                opts.setWorkingDir(getProject().getLayout().getProjectDirectory());
                opts.setExecutable(getJavaLauncher().get().getExecutablePath().getAsFile().getAbsolutePath());
                opts.setJvmArgs(forkOpts.getJvmArgs());
                opts.setMinHeapSize(forkOpts.getMemoryInitialSize());
                opts.setMaxHeapSize(forkOpts.getMemoryMaximumSize());
            });
        }) : getWorkerExecutor().classLoaderIsolation(spec -> {
            spec.getClasspath().from(getMinaCompilerClasspath());
        });

        workQueue.submit(MinaCompileAction.class, params -> {
            params.getCompilerClassName().set("org.mina_lang.cli.MinaCommandLine");
            params.getMinaCompileOptions().set(getMinaCompileOptions());
            params.getDestinationDirectory().set(getDestinationDirectory());
            params.getClasspath().from(getClasspath());
            params.getSourceFiles().from(getSource());
        });
    }
}

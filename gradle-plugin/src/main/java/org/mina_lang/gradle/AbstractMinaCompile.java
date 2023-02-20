package org.mina_lang.gradle;


import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.internal.tasks.compile.CompilerForkUtils;
import org.gradle.api.internal.tasks.compile.HasCompileOptions;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.work.DisableCachingByDefault;;

@DisableCachingByDefault(because = "Abstract super-class, not to be instantiated directly")
public abstract class AbstractMinaCompile extends AbstractCompile implements HasCompileOptions {
    private BaseMinaCompileOptions minaCompileOptions;
    private CompileOptions compileOptions;
    private final Property<JavaLauncher> javaLauncher;

    protected AbstractMinaCompile() {
        ObjectFactory objectFactory = getObjectFactory();
        JavaToolchainService javaToolchainService = getJavaToolchainService();
        this.minaCompileOptions = objectFactory.newInstance(MinaCompileOptions.class);
        this.compileOptions = objectFactory.newInstance(CompileOptions.class);
        this.javaLauncher = objectFactory.property(JavaLauncher.class).convention(javaToolchainService.launcherFor(it -> {}));
        CompilerForkUtils.doNotCacheIfForkingViaExecutable(compileOptions, getOutputs());
    }

    @Nested
    public BaseMinaCompileOptions getMinaCompileOptions() {
        return minaCompileOptions;
    }

    @Nested
    public CompileOptions getOptions() {
        return compileOptions;
    }

    @Nested
    public Property<JavaLauncher> getJavaLauncher() {
        return javaLauncher;
    }

    abstract protected Compiler<DefaultMinaCompileSpec> getCompiler(DefaultMinaCompileSpec spec);

    @TaskAction
    public void compile() {
        DefaultMinaCompileSpec spec = createSpec();
        Compiler<DefaultMinaCompileSpec> compiler = getCompiler(spec);
        compiler.execute(spec);
    }

    protected DefaultMinaCompileSpec createSpec() {
        File javaExecutable = getJavaLauncher().get().getExecutablePath().getAsFile();
        DefaultMinaCompileSpec spec = new DefaultMinaCompileSpec(javaExecutable);
        spec.setSourceFiles(getSource().getFiles());
        spec.setDestinationDir(getDestinationDirectory().getAsFile().get());
        spec.setWorkingDir(getProject().getLayout().getProjectDirectory().getAsFile());
        spec.setTempDir(getTemporaryDir());
        spec.setCompileClasspath(List.copyOf(getClasspath().getFiles()));
        spec.setSourceCompatibility(getSourceCompatibility());
        spec.setTargetCompatibility(getTargetCompatibility());
        spec.setMinaCompileOptions(new MinimalMinaCompileOptions(getMinaCompileOptions()));
        return spec;
    }

    @Inject
    protected abstract ObjectFactory getObjectFactory();

    @Inject
    protected abstract JavaToolchainService getJavaToolchainService();
}

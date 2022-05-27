package org.mina_lang.gradle;


import org.gradle.api.internal.tasks.compile.HasCompileOptions;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.internal.impldep.com.google.common.collect.ImmutableList;
import org.gradle.language.base.internal.compile.Compiler;;

public abstract class AbstractMinaCompile extends AbstractCompile implements HasCompileOptions {
    private BaseMinaCompileOptions minaCompileOptions;
    private CompileOptions compileOptions;

    public AbstractMinaCompile(BaseMinaCompileOptions minaCompileOptions, ObjectFactory objectFactory) {
        this.minaCompileOptions = minaCompileOptions;
        this.compileOptions = objectFactory.newInstance(CompileOptions.class);
    }

    @Nested
    public BaseMinaCompileOptions getMinaCompileOptions() {
        return minaCompileOptions;
    }

    @Nested
    public CompileOptions getOptions() {
        return compileOptions;
    }

    abstract protected Compiler<DefaultMinaCompileSpec> getCompiler(DefaultMinaCompileSpec spec);

    @TaskAction
    public void compile() {
        DefaultMinaCompileSpec spec = createSpec();
        Compiler<DefaultMinaCompileSpec> compiler = getCompiler(spec);
        compiler.execute(spec);
    }

    protected DefaultMinaCompileSpec createSpec() {
        DefaultMinaCompileSpec spec = new DefaultMinaCompileSpec();
        spec.setSourceFiles(getSource().getFiles());
        spec.setDestinationDir(getDestinationDirectory().getAsFile().get());
        spec.setWorkingDir(getProject().getLayout().getProjectDirectory().getAsFile());
        spec.setTempDir(getTemporaryDir());
        spec.setCompileClasspath(ImmutableList.copyOf(getClasspath()));
        spec.setSourceCompatibility(getSourceCompatibility());
        spec.setTargetCompatibility(getTargetCompatibility());
        spec.setMinaCompileOptions(new MinimalMinaCompileOptions(getMinaCompileOptions()));
        return spec;
    }
}

package org.mina_lang.gradle;

import java.io.File;

import org.gradle.api.internal.tasks.compile.DefaultJvmLanguageCompileSpec;

public class DefaultMinaCompileSpec extends DefaultJvmLanguageCompileSpec implements MinaCompileSpec {
    private MinimalMinaCompileOptions options;
    private Iterable<File> minaClasspath;
    private final File javaExecutable;

    public DefaultMinaCompileSpec(File javaExecutable) {
        this.javaExecutable = javaExecutable;
    }

    @Override
    public MinimalMinaCompileOptions getMinaCompileOptions() {
        return options;
    }

    public void setMinaCompileOptions(MinimalMinaCompileOptions options) {
        this.options = options;
    }

    public Iterable<File> getMinaClasspath() {
        return minaClasspath;
    }

    public void setMinaClasspath(Iterable<File> minaClasspath) {
        this.minaClasspath = minaClasspath;
    }

    @Override
    public File getJavaExecutable() {
        return javaExecutable;
    }
}

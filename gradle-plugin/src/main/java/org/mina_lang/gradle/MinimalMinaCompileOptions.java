package org.mina_lang.gradle;

import java.util.List;

import javax.annotation.Nullable;

import org.gradle.internal.impldep.com.google.common.collect.ImmutableList;

public class MinimalMinaCompileOptions {
    private List<String> additionalParameters;
    private MinimalMinaCompilerDaemonForkOptions forkOptions;

    public MinimalMinaCompileOptions(BaseMinaCompileOptions compileOptions) {
        this.additionalParameters = compileOptions.getAdditionalParameters() == null ? null
                : ImmutableList.copyOf(compileOptions.getAdditionalParameters());
        this.forkOptions = new MinimalMinaCompilerDaemonForkOptions(compileOptions.getForkOptions());
    }

    @Nullable
    public List<String> getAdditionalParameters() {
        return additionalParameters;
    }

    public void setAdditionalParameters(@Nullable List<String> additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    public MinimalMinaCompilerDaemonForkOptions getForkOptions() {
        return forkOptions;
    }

    public void setForkOptions(MinimalMinaCompilerDaemonForkOptions forkOptions) {
        this.forkOptions = forkOptions;
    }
}

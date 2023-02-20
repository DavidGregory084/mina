package org.mina_lang.gradle;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import org.gradle.workers.internal.KeepAliveMode;

public class MinimalMinaCompileOptions implements Serializable {
    private List<String> additionalParameters;
    private MinimalMinaCompilerDaemonForkOptions forkOptions;
    private KeepAliveMode keepAliveMode;

    public MinimalMinaCompileOptions(BaseMinaCompileOptions compileOptions) {
        this.additionalParameters = compileOptions.getAdditionalParameters() == null ? null
                : List.copyOf(compileOptions.getAdditionalParameters());
        this.forkOptions = new MinimalMinaCompilerDaemonForkOptions(compileOptions.getForkOptions());
        this.keepAliveMode = compileOptions.getKeepAliveMode().get();
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

    public KeepAliveMode getKeepAliveMode() {
        return keepAliveMode;
    }
}

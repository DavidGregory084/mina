package org.mina_lang.gradle;

import org.gradle.api.internal.tasks.compile.MinimalCompilerDaemonForkOptions;

public class MinimalMinaCompilerDaemonForkOptions extends MinimalCompilerDaemonForkOptions {

    public MinimalMinaCompilerDaemonForkOptions(MinaForkOptions forkOptions) {
        super(forkOptions);
        setJvmArgs(forkOptions.getAllJvmArgs());
    }
    
}

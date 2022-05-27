package org.mina_lang.gradle;

import org.gradle.api.internal.tasks.compile.DefaultJvmLanguageCompileSpec;

public class DefaultMinaCompileSpec extends DefaultJvmLanguageCompileSpec implements MinaCompileSpec {
    private MinimalMinaCompileOptions options;

    @Override
    public MinimalMinaCompileOptions getMinaCompileOptions() {
        return options;
    }

    public void setMinaCompileOptions(MinimalMinaCompileOptions options) {
        this.options = options;
    }
}

package org.mina_lang.gradle;

import java.util.List;

import javax.annotation.Nullable;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.compile.AbstractOptions;

public class BaseMinaCompileOptions extends AbstractOptions {
    private static final long serialVersionUID = 0;

    private List<String> additionalParameters;
    private MinaForkOptions forkOptions = new MinaForkOptions();

    @Nullable @Optional @Input
    public List<String> getAdditionalParameters() {
        return additionalParameters;
    }

    public void setAdditionalParameters(@Nullable List<String> additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    @Nested
    public MinaForkOptions getForkOptions() {
        return forkOptions;
    }

    public void setForkOptions(MinaForkOptions forkOptions) {
        this.forkOptions = forkOptions;
    }
}

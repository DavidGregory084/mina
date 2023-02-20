package org.mina_lang.gradle;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.compile.AbstractOptions;
import org.gradle.workers.internal.KeepAliveMode;

public class BaseMinaCompileOptions extends AbstractOptions {
    private static final long serialVersionUID = 0;

    private List<String> additionalParameters;
    private MinaForkOptions forkOptions = new MinaForkOptions();
    private Property<KeepAliveMode> keepAliveMode = getObjectFactory().property(KeepAliveMode.class);

    @Inject
    protected ObjectFactory getObjectFactory() {
        throw new UnsupportedOperationException();
    }

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

    @Input
    public Property<KeepAliveMode> getKeepAliveMode() {
        return keepAliveMode;
    }
}

package org.mina_lang.gradle;

import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public abstract class DefaultMinaExtension implements MinaExtension {
    private final Property<String> minaVersion;

    @Inject
    public DefaultMinaExtension(ObjectFactory objectFactory) {
        this.minaVersion = objectFactory.property(String.class);
    }

    @Override
    public Property<String> getMinaVersion() {
        return minaVersion;
    }
}

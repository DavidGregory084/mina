package org.mina_lang.gradle;

import org.gradle.api.provider.Property;

public interface MinaExtension {
    Property<String> getMinaVersion();
}

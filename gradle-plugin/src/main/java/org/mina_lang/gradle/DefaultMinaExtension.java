/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

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

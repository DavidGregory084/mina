/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle;

import org.gradle.api.provider.Property;

public interface MinaExtension {
    Property<String> getMinaVersion();
}

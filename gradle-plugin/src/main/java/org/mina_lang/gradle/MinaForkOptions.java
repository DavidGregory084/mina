/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle;

import org.gradle.api.tasks.compile.ProviderAwareCompilerDaemonForkOptions;

public abstract class MinaForkOptions extends ProviderAwareCompilerDaemonForkOptions {
    private static final long serialVersionUID = 0L;
}

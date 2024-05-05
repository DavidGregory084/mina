/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle.compiler;

import org.mina_lang.gradle.MinaCompileParameters;

public interface MinaCompiler {
    void compile(MinaCompileParameters compileParameters);
}

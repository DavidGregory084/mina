/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.gradle;

public class MinaCompilationException extends RuntimeException {
    public MinaCompilationException(String message) {
        super(message);
    }
}

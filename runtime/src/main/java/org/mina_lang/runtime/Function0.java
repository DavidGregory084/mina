/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.runtime;

@FunctionalInterface
public interface Function0<R> extends Function<R> {
    public R apply();
}

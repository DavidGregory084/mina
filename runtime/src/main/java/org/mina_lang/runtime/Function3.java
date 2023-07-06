/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.runtime;

@FunctionalInterface
public interface Function3<A0, A1, A2, R> extends Function<R> {
    public R apply(A0 arg0, A1 arg1, A2 arg2);
}

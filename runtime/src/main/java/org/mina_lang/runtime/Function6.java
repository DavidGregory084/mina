/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.runtime;

@FunctionalInterface
public interface Function6<A0, A1, A2, A3, A4, A5, R> extends Function<R> {
    public R apply(A0 arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, A5 arg5);
}

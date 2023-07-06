/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.cli.logging;

public class FaintCompositeConverter<E> extends ColourSupportCheckingCompositeConverter<E> {
    @Override
    protected String getForegroundColorCode(E event) {
        return "2";
    }
}

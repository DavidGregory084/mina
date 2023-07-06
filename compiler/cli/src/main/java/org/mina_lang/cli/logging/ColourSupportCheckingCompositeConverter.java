/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.cli.logging;

import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;
import org.mina_lang.cli.ColourSupport;

abstract class ColourSupportCheckingCompositeConverter<E> extends ForegroundCompositeConverterBase<E> {
    @Override
    protected String transform(E event, String in) {
        if (!ColourSupport.isSupported()) {
            return in;
        } else {
            return super.transform(event, in);
        }
    }
}

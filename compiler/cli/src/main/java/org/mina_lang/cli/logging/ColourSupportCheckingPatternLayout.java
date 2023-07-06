/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.cli.logging;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.core.pattern.IdentityCompositeConverter;
import org.mina_lang.cli.ColourSupport;

public class ColourSupportCheckingPatternLayout extends PatternLayout {
    static {
        if (!ColourSupport.isSupported()) {
            DEFAULT_CONVERTER_MAP.put("black", IdentityCompositeConverter.class.getName());
            DEFAULT_CONVERTER_MAP.put("red", IdentityCompositeConverter.class.getName());
            DEFAULT_CONVERTER_MAP.put("green", IdentityCompositeConverter.class.getName());
            DEFAULT_CONVERTER_MAP.put("yellow", IdentityCompositeConverter.class.getName());
            DEFAULT_CONVERTER_MAP.put("blue", IdentityCompositeConverter.class.getName());
            DEFAULT_CONVERTER_MAP.put("magenta", IdentityCompositeConverter.class.getName());
            DEFAULT_CONVERTER_MAP.put("cyan", IdentityCompositeConverter.class.getName());
            DEFAULT_CONVERTER_MAP.put("white", IdentityCompositeConverter.class.getName());
            DEFAULT_CONVERTER_MAP.put("gray", IdentityCompositeConverter.class.getName());
            DEFAULT_CONVERTER_MAP.put("boldRed", IdentityCompositeConverter.class.getName());
            DEFAULT_CONVERTER_MAP.put("boldGreen", IdentityCompositeConverter.class.getName());
            DEFAULT_CONVERTER_MAP.put("boldYellow", IdentityCompositeConverter.class.getName());
            DEFAULT_CONVERTER_MAP.put("boldBlue", IdentityCompositeConverter.class.getName());
            DEFAULT_CONVERTER_MAP.put("boldMagenta", IdentityCompositeConverter.class.getName());
            DEFAULT_CONVERTER_MAP.put("boldCyan", IdentityCompositeConverter.class.getName());
            DEFAULT_CONVERTER_MAP.put("boldWhite", IdentityCompositeConverter.class.getName());
            DEFAULT_CONVERTER_MAP.put("highlight", IdentityCompositeConverter.class.getName());
        }
    }
}

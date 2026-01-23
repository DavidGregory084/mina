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
            DEFAULT_CONVERTER_SUPPLIER_MAP.put("black", IdentityCompositeConverter::new);
            DEFAULT_CONVERTER_SUPPLIER_MAP.put("red", IdentityCompositeConverter::new);
            DEFAULT_CONVERTER_SUPPLIER_MAP.put("green", IdentityCompositeConverter::new);
            DEFAULT_CONVERTER_SUPPLIER_MAP.put("yellow", IdentityCompositeConverter::new);
            DEFAULT_CONVERTER_SUPPLIER_MAP.put("blue", IdentityCompositeConverter::new);
            DEFAULT_CONVERTER_SUPPLIER_MAP.put("magenta", IdentityCompositeConverter::new);
            DEFAULT_CONVERTER_SUPPLIER_MAP.put("cyan", IdentityCompositeConverter::new);
            DEFAULT_CONVERTER_SUPPLIER_MAP.put("white", IdentityCompositeConverter::new);
            DEFAULT_CONVERTER_SUPPLIER_MAP.put("gray", IdentityCompositeConverter::new);
            DEFAULT_CONVERTER_SUPPLIER_MAP.put("boldRed", IdentityCompositeConverter::new);
            DEFAULT_CONVERTER_SUPPLIER_MAP.put("boldGreen", IdentityCompositeConverter::new);
            DEFAULT_CONVERTER_SUPPLIER_MAP.put("boldYellow", IdentityCompositeConverter::new);
            DEFAULT_CONVERTER_SUPPLIER_MAP.put("boldBlue", IdentityCompositeConverter::new);
            DEFAULT_CONVERTER_SUPPLIER_MAP.put("boldMagenta", IdentityCompositeConverter::new);
            DEFAULT_CONVERTER_SUPPLIER_MAP.put("boldCyan", IdentityCompositeConverter::new);
            DEFAULT_CONVERTER_SUPPLIER_MAP.put("boldWhite", IdentityCompositeConverter::new);
            DEFAULT_CONVERTER_SUPPLIER_MAP.put("highlight", IdentityCompositeConverter::new);
        }
    }
}

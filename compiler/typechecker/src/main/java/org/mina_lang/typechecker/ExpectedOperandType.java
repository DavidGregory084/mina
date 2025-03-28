/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.typechecker;

import com.opencastsoftware.prettier4j.Doc;
import org.mina_lang.common.types.MonoType;
import org.mina_lang.common.types.SortPrinter;
import org.mina_lang.common.types.Type;

public enum ExpectedOperandType {
    NUMERIC,
    INTEGRAL,
    INTEGER,
    BOOLEAN,
    INTEGRAL_OR_BOOLEAN;

    public Doc message(SortPrinter printer) {
        return switch (this) {
            case NUMERIC -> Doc.text("A numeric type");
            case INTEGRAL -> Doc.text("An integral type");
            case BOOLEAN -> Type.BOOLEAN.accept(printer);
            case INTEGER -> Type.INT.accept(printer);
            case INTEGRAL_OR_BOOLEAN -> Doc.text("An integral or boolean type");
        };
    }

    public MonoType[] validTypes() {
        return switch (this) {
            case NUMERIC -> new MonoType[] { Type.INT, Type.LONG, Type.FLOAT, Type.DOUBLE, Type.BOOLEAN };
            case INTEGRAL -> new MonoType[] { Type.INT, Type.LONG };
            case INTEGER -> new MonoType[] { Type.INT };
            case BOOLEAN -> new MonoType[] { Type.BOOLEAN };
            case INTEGRAL_OR_BOOLEAN -> new MonoType[] { Type.INT, Type.LONG, Type.BOOLEAN };
        };
    }
}

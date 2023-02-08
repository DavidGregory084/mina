package org.mina_lang.cli.logging;

public class FaintCompositeConverter<E> extends ColourSupportCheckingCompositeConverter<E> {
    @Override
    protected String getForegroundColorCode(E event) {
        return "2";
    }
}

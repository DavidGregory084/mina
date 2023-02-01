package org.mina_lang.cli.logging;

import org.mina_lang.cli.ColourSupport;

import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

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

package org.mina_lang.syntax;

import org.mina_lang.common.Range;

public record Meta<A>(Range range, A meta) {
    public static Meta<Void> empty(Range range) {
        return new Meta<Void>(range, null);
    }
}

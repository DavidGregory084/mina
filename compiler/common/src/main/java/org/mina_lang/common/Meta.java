/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common;

import com.opencastsoftware.yvette.Range;

public record Meta<A>(Range range, A meta) {
    public static Meta<Void> EMPTY = new Meta<>(Range.EMPTY, null);

    public Meta<A> withRange(Range range) {
        return new Meta<A>(range, meta());
    }

    public <B> Meta<B> withMeta(B meta) {
        return new Meta<B>(range(), meta);
    }

    public static Meta<Void> of(Range range) {
        return new Meta<Void>(range, null);
    }

    public static <A> Meta<A> of(A meta) {
        return new Meta<A>(Range.EMPTY, meta);
    }
}

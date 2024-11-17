/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;

import java.util.Arrays;

public record HigherKind(ImmutableList<Kind> argKinds, Kind resultKind) implements Kind {

    public HigherKind(ImmutableList<Kind> kinds) {
        this(kinds.take(kinds.size() - 1), kinds.getLast());
    }

    public HigherKind(Kind... kinds) {
        this(
                Lists.immutable.of(Arrays.copyOfRange(kinds, 0, kinds.length - 1)),
                kinds[kinds.length - 1]);
    }

    @Override
    public <A> A accept(KindFolder<A> visitor) {
        return visitor.visitHigherKind(this);
    }

    @Override
    public void accept(KindVisitor visitor) {
        visitor.visitHigherKind(this);
    }

    @Override
    public Kind accept(KindTransformer visitor) {
        return visitor.visitHigherKind(this);
    }
}

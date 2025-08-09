/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

import java.util.Arrays;
import java.util.List;

public record HigherKind(List<Kind> argKinds, Kind resultKind) implements Kind {

    public HigherKind(List<Kind> kinds) {
        this(kinds.subList(0, kinds.size() - 1), kinds.get(kinds.size() - 1));
    }

    public HigherKind(Kind... kinds) {
        this(
                Arrays.asList(Arrays.copyOfRange(kinds, 0, kinds.length - 1)),
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

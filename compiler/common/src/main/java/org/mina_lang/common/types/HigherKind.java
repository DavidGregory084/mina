package org.mina_lang.common.types;

import org.eclipse.collections.api.list.ImmutableList;

public record HigherKind(ImmutableList<Kind> argKinds, Kind resultKind) implements Kind {

    @Override
    public <A> A accept(KindFolder<A> visitor) {
        return visitor.visitHigherKind(this);
    }

    @Override
    public Kind accept(KindTransformer visitor) {
        return visitor.visitHigherKind(this);
    }
}

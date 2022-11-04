package org.mina_lang.common.types;

import org.eclipse.collections.api.list.ImmutableList;

public record TypeApply(Type type, ImmutableList<Type> typeArguments, Kind kind) implements MonoType {

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitTypeApply(this);
    }

    @Override
    public TypeApply accept(TypeTransformer visitor) {
        return visitor.visitTypeApply(this);
    }
}

package org.mina_lang.common.types;

import org.eclipse.collections.api.list.ImmutableList;

public record TypeLambda(ImmutableList<TypeVar> args, Type body, Kind kind) implements PolyType {

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visitTypeLambda(this);
    }

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitTypeLambda(this);
    }

    @Override
    public TypeLambda accept(TypeTransformer visitor) {
        return visitor.visitTypeLambda(this);
    }
}

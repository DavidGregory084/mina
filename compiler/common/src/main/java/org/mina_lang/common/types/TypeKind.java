package org.mina_lang.common.types;

public enum TypeKind implements Kind {
    INSTANCE;

    @Override
    public <A> A accept(KindFolder<A> visitor) {
        return visitor.visitTypeKind(INSTANCE);
    }

    @Override
    public Kind accept(KindTransformer visitor) {
        return visitor.visitTypeKind(INSTANCE);
    }
}

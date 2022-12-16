package org.mina_lang.common.types;

public record PropositionType(Type type, Proposition property) implements PolyType {
    @Override
    public Kind kind() {
        return type.kind();
    }

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitPropositionType(this);
    }

    @Override
    public PropositionType accept(TypeTransformer visitor) {
        return visitor.visitPropositionType(this);
    }
}

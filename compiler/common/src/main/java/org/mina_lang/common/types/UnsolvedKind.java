package org.mina_lang.common.types;

public record UnsolvedKind(int id) implements Kind {
    public String name() {
        var div = (id / 26) + 1;
        var rem = id % 26;
        var prefixChar = (char) ('A' + rem);
        return String.valueOf(prefixChar) + String.valueOf(div);
    }

    @Override
    public <A> A accept(KindFolder<A> visitor) {
        return visitor.visitUnsolvedKind(this);
    }

    @Override
    public Kind accept(KindTransformer visitor) {
        return visitor.visitUnsolvedKind(this);
    }

    public boolean isFreeIn(Kind kind) {
        return this.accept(new KindFolder<Boolean>() {
            @Override
            public Boolean visitTypeKind(TypeKind typ) {
                return false;
            }

            @Override
            public Boolean visitUnsolvedKind(UnsolvedKind unsolved) {
                return id() == unsolved.id();
            }

            @Override
            public Boolean visitHigherKind(HigherKind higher) {
                var occursInArgs = higher
                        .argKinds()
                        .anySatisfy(arg -> arg.accept(this));
                var occursInResult = higher
                        .resultKind()
                        .accept(this);
                return occursInArgs || occursInResult;
            }
        });
    }
}

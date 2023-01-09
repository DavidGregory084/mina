package org.mina_lang.common.types;

public record UnsolvedType(int id, Kind kind) implements MonoType {
    public String name() {
        var div = (id / 26) + 1;
        var rem = id % 26;
        var prefixChar = (char) ('A' + rem);
        return String.valueOf(prefixChar) + String.valueOf(div);
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visitUnsolvedType(this);
    }

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitUnsolvedType(this);
    }

    @Override
    public MonoType accept(TypeTransformer visitor) {
        return visitor.visitUnsolvedType(this);
    }

    public boolean isFreeIn(Type type) {
        return type.accept(new TypeFolder<Boolean>() {
            @Override
            public Boolean visitTypeLambda(TypeLambda tyLam) {
                return tyLam.body().accept(this);
            }

            @Override
            public Boolean visitPropositionType(PropositionType propType) {
                return propType.type().accept(this);
            }

            @Override
            public Boolean visitImplicationType(ImplicationType implType) {
                return implType.impliedType().accept(this);
            }

            @Override
            public Boolean visitTypeConstructor(TypeConstructor tyCon) {
                return false;
            }

            @Override
            public Boolean visitBuiltInType(BuiltInType primTy) {
                return false;
            }

            @Override
            public Boolean visitTypeApply(TypeApply tyApp) {
                var occursInType = tyApp.type().accept(this);
                var occursInArgs = tyApp.typeArguments()
                        .anySatisfy(arg -> arg.accept(this));
                return occursInType || occursInArgs;
            }

            @Override
            public Boolean visitForAllVar(ForAllVar forall) {
                return false;
            }

            @Override
            public Boolean visitExistsVar(ExistsVar exists) {
                return false;
            }

            @Override
            public Boolean visitUnsolvedType(UnsolvedType unsolved) {
                return id() == unsolved.id();
            }
        });
    }
}

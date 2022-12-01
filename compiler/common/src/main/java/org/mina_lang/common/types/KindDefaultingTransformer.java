package org.mina_lang.common.types;

public class KindDefaultingTransformer extends KindSubstitutionTransformer {

    public KindDefaultingTransformer() {
        super();
    }

    public KindDefaultingTransformer(UnionFind<Kind> substitution) {
        super(substitution);
    }

    @Override
    public Kind visitUnsolvedKind(UnsolvedKind unsolved) {
        var solution = substitution.find(unsolved);
        return solution.equals(unsolved) ? TypeKind.INSTANCE : solution.accept(this);
    }
}

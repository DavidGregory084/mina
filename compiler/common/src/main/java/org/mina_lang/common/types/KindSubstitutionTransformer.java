/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public class KindSubstitutionTransformer implements KindTransformer {

    protected UnionFind<Kind> substitution;

    public KindSubstitutionTransformer() {
    }

    public KindSubstitutionTransformer(UnionFind<Kind> substitution) {
        this.substitution = substitution;
    }

    @Override
    public TypeKind visitTypeKind(TypeKind typ) {
        return typ;
    }

    @Override
    public Kind visitUnsolvedKind(UnsolvedKind unsolved) {
        var solution = substitution.find(unsolved);
        return solution.equals(unsolved) ? solution : solution.accept(this);
    }

    @Override
    public HigherKind visitHigherKind(HigherKind higher) {
        return new HigherKind(
            higher.argKinds().collect(argKind -> argKind.accept(this)),
            higher.resultKind().accept(this));
    }
}

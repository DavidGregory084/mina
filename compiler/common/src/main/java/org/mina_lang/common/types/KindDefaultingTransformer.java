/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
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
        if (!solution.equals(unsolved)) {
            return solution.accept(this);
        } else {
            substitution.union(unsolved, TypeKind.INSTANCE);
            return TypeKind.INSTANCE;
        }
    }
}

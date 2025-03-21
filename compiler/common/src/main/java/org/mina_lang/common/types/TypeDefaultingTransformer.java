/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public class TypeDefaultingTransformer extends TypeSubstitutionTransformer {
    private final UnsolvedVariableSupply varSupply;

    public TypeDefaultingTransformer(UnionFind<MonoType> typeSubstitution, KindSubstitutionTransformer kindTransformer, UnsolvedVariableSupply varSupply) {
        super(typeSubstitution, kindTransformer);
        this.varSupply = varSupply;
    }

    @Override
    public MonoType visitUnsolvedType(UnsolvedType unsolved) {
        var solution = typeSubstitution.find(unsolved);
        if (!solution.equals(unsolved)) {
            return solution.accept(this);
        } else {
            var newKind = unsolved.kind().accept(kindTransformer);
            var newSynthetic = varSupply.newSyntheticVar(newKind);
            typeSubstitution.union(unsolved, newSynthetic);
            return newSynthetic;
        }
    }
}

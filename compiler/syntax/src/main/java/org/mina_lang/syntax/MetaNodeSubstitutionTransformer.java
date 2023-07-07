/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.types.*;

public class MetaNodeSubstitutionTransformer implements MetaNodeMetaTransformer<Attributes, Attributes> {

    private SortSubstitutionTransformer sortTransformer;

    public MetaNodeSubstitutionTransformer(SortSubstitutionTransformer sortTransformer) {
        this.sortTransformer = sortTransformer;
    }


    public MetaNodeSubstitutionTransformer(UnionFind<MonoType> typeSubstitution, UnionFind<Kind> kindSubstitution) {
        this.sortTransformer = new SortSubstitutionTransformer(typeSubstitution, kindSubstitution);
    }

    @Override
    public Meta<Attributes> updateMeta(Meta<Attributes> meta) {
        var substituted = meta.meta().sort().accept(sortTransformer);
        var attributes = meta.meta().withSort(substituted);
        return meta.withMeta(attributes);
    }
}

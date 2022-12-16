package org.mina_lang.syntax;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.types.KindSubstitutionTransformer;

public class TypeNodeSubstitutionTransformer implements MetaKindSubstitutionTransformer, TypeNodeMetaTransformer<Attributes, Attributes> {

    private KindSubstitutionTransformer kindTransformer;

    public KindSubstitutionTransformer kindTransformer() {
        return kindTransformer;
    }

    public TypeNodeSubstitutionTransformer(KindSubstitutionTransformer kindTransformer) {
        this.kindTransformer = kindTransformer;
    }
}

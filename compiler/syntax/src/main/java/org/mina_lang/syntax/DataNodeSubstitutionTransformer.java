package org.mina_lang.syntax;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.types.KindSubstitutionTransformer;

public class DataNodeSubstitutionTransformer extends TypeNodeSubstitutionTransformer implements DataNodeMetaTransformer<Attributes, Attributes> {

    public DataNodeSubstitutionTransformer(KindSubstitutionTransformer kindTransformer) {
        super(kindTransformer);
    }
}

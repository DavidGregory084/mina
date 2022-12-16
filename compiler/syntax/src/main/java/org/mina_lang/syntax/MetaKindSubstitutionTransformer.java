package org.mina_lang.syntax;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.types.Kind;
import org.mina_lang.common.types.KindSubstitutionTransformer;

public interface MetaKindSubstitutionTransformer extends MetaTransformer<Attributes, Attributes> {

    KindSubstitutionTransformer kindTransformer();

    @Override
    default Meta<Attributes> updateMeta(Meta<Attributes> meta) {
        var updatedKind = ((Kind) meta.meta().sort()).accept(kindTransformer());
        var attributes = meta.meta().withSort(updatedKind);
        return meta.withMeta(attributes);
    }
}

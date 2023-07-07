/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.types.Kind;
import org.mina_lang.common.types.Type;
import org.mina_lang.common.types.TypeSubstitutionTransformer;

public interface MetaTypeSubstitutionTransformer extends MetaKindSubstitutionTransformer {
    TypeSubstitutionTransformer typeTransformer();

    @Override
    default Meta<Attributes> updateMeta(Meta<Attributes> meta) {
        if (meta.meta().sort() instanceof Kind kind) {
            var updatedKind = kind.accept(kindTransformer());
            var attributes = meta.meta().withSort(updatedKind);
            return meta.withMeta(attributes);
        } else if (meta.meta().sort() instanceof Type type) {
            var updatedType = type.accept(typeTransformer());
            var attributes = meta.meta().withSort(updatedType);
            return meta.withMeta(attributes);
        }

        return null;
    }
}

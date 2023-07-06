/*
 * SPDX-FileCopyrightText:  © 2022 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

public enum TypeKind implements Kind {
    INSTANCE;

    @Override
    public <A> A accept(KindFolder<A> visitor) {
        return visitor.visitTypeKind(INSTANCE);
    }

    @Override
    public Kind accept(KindTransformer visitor) {
        return visitor.visitTypeKind(INSTANCE);
    }
}

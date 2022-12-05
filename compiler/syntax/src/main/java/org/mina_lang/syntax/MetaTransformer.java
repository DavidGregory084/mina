package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public interface MetaTransformer<A, B> {
    abstract Meta<B> updateMeta(Meta<A> meta);
}

/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;
import org.mina_lang.common.names.LocalName;

import java.util.Optional;

public record ParamNode<A> (Meta<A> meta, String name, Optional<TypeNode<A>> typeAnnotation) implements MetaNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        typeAnnotation.ifPresent(tyAnn -> tyAnn.accept(visitor));
        visitor.visitParam(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitParam(this);

        var result = visitor.visitParam(
                meta(),
                name(),
                typeAnnotation().map(visitor::visitType));

        visitor.postVisitParam(this);

        return result;
    }

    @Override
    public <B> ParamNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitParam(this);

        var result = visitor.visitParam(
                meta(),
                name(),
                typeAnnotation().map(visitor::visitType));

        visitor.postVisitParam(result);

        return result;
    }

    public LocalName getName(int localNameDepth) {
        return new LocalName(name(), localNameDepth);
    }
}

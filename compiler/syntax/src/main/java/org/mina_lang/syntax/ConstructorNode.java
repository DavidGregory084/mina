/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.*;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.DataName;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.common.names.QualifiedName;

import java.util.List;
import java.util.Optional;

public record ConstructorNode<A> (Meta<A> meta, String name, List<ConstructorParamNode<A>> params,
        Optional<TypeNode<A>> type) implements MetaNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        params.forEach(param -> param.accept(visitor));
        type.ifPresent(typ -> typ.accept(visitor));
        visitor.visitConstructor(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        return accept((DataNodeFolder<A, B>) visitor);
    }

    public <B> B accept(DataNodeFolder<A, B> visitor) {
        visitor.preVisitConstructor(this);

        var result = visitor.visitConstructor(
                meta(),
                name(),
                params().stream().map(param -> param.accept(visitor)).toList(),
                type().map(visitor::visitType));

        visitor.postVisitConstructor(result);

        return result;
    }

    @Override
    public <B> ConstructorNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        return accept((DataNodeTransformer<A, B>) visitor);
    }

    public <B> ConstructorNode<B> accept(DataNodeTransformer<A, B> visitor) {
        visitor.preVisitConstructor(this);

        var result = visitor.visitConstructor(
                meta(),
                name(),
                params().stream().map(param -> param.accept(visitor)).toList(),
                type().map(visitor::visitType));

        visitor.postVisitConstructor(result);

        return result;
    }

    public ConstructorName getName(DataName enclosing, NamespaceName namespace) {
        return new ConstructorName(enclosing, new QualifiedName(namespace, name));
    }
}

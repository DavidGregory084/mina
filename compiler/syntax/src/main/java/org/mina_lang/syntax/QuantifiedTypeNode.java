/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

import java.util.List;

public record QuantifiedTypeNode<A> (Meta<A> meta, List<TypeVarNode<A>> args, TypeNode<A> body)
        implements TypeNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        args.forEach(arg -> arg.accept(visitor));
        body.accept(visitor);
        visitor.visitQuantifiedType(this);
    }

    @Override
    public <B> B accept(TypeNodeFolder<A, B> visitor) {
        visitor.preVisitQuantifiedType(this);

        var result = visitor.visitQuantifiedType(
                meta(),
                args().stream().map(visitor::visitTypeVar).toList(),
                visitor.visitType(body()));

        visitor.postVisitQuantifiedType(result);

        return result;
    }

    @Override
    public <B> QuantifiedTypeNode<B> accept(TypeNodeTransformer<A, B> visitor) {
        visitor.preVisitQuantifiedType(this);

        var result = visitor.visitQuantifiedType(
                meta(),
                args().stream().map(visitor::visitTypeVar).toList(),
                visitor.visitType(body()));

        visitor.postVisitQuantifiedType(result);

        return result;
    }
}

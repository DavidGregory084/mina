/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

import java.util.List;

public record FunTypeNode<A> (Meta<A> meta, List<TypeNode<A>> argTypes, TypeNode<A> returnType)
        implements TypeNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        argTypes.forEach(argTy -> argTy.accept(visitor));
        returnType.accept(visitor);
        visitor.visitFunType(this);
    }

    @Override
    public <B> B accept(TypeNodeFolder<A, B> visitor) {
        visitor.preVisitFunType(this);

        var result = visitor.visitFunType(
                meta(),
                argTypes().stream().map(visitor::visitType).toList(),
                visitor.visitType(returnType()));

        visitor.postVisitFunType(result);

        return result;
    }

    @Override
    public <B> FunTypeNode<B> accept(TypeNodeTransformer<A, B> visitor) {
        visitor.preVisitFunType(this);

        var result = visitor.visitFunType(
                meta(),
                argTypes().stream().map(visitor::visitType).toList(),
                visitor.visitType(returnType()));

        visitor.postVisitFunType(result);

        return result;
    }
}

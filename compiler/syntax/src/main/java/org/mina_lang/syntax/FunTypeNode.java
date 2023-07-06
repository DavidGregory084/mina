/*
 * SPDX-FileCopyrightText:  © 2022 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public record FunTypeNode<A> (Meta<A> meta, ImmutableList<TypeNode<A>> argTypes, TypeNode<A> returnType)
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
                argTypes().collect(visitor::visitType),
                visitor.visitType(returnType()));

        visitor.postVisitFunType(result);

        return result;
    }

    @Override
    public <B> FunTypeNode<B> accept(TypeNodeTransformer<A, B> visitor) {
        visitor.preVisitFunType(this);

        var result = visitor.visitFunType(
                meta(),
                argTypes().collect(visitor::visitType),
                visitor.visitType(returnType()));

        visitor.postVisitFunType(result);

        return result;
    }
}

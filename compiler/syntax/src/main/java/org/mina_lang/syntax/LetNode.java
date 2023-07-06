/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;
import org.mina_lang.common.names.LetName;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.common.names.QualifiedName;

import java.util.Optional;

public record LetNode<A> (Meta<A> meta, String name, Optional<TypeNode<A>> type, ExprNode<A> expr)
        implements DeclarationNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        type.ifPresent(typ -> typ.accept(visitor));
        expr.accept(visitor);
        visitor.visitLet(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitLet(this);

        var result = visitor.visitLet(
                meta(),
                name(),
                type().map(visitor::visitType),
                visitor.visitExpr(expr()));

        visitor.postVisitLet(this);

        return result;
    }

    @Override
    public <B> LetNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitLet(this);

        var result = visitor.visitLet(
                meta(),
                name(),
                type().map(visitor::visitType),
                visitor.visitExpr(expr()));

        visitor.postVisitLet(result);

        return result;
    }

    public LetName getName(NamespaceName namespace) {
        return new LetName(new QualifiedName(namespace, name));
    }
}

/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

import java.util.List;

public record ApplyNode<A> (Meta<A> meta, ExprNode<A> expr, List<ExprNode<A>> args) implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        expr.accept(visitor);
        args.forEach(arg -> arg.accept(visitor));
        visitor.visitApply(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitApply(this);

        var result = visitor.visitApply(
                meta(),
                visitor.visitExpr(expr()),
                args().stream().map(visitor::visitExpr).toList());

        visitor.postVisitApply(this);

        return result;
    }

    @Override
    public <B> ApplyNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitApply(this);

        var result = visitor.visitApply(
                meta(),
                visitor.visitExpr(expr()),
                args().stream().map(visitor::visitExpr).toList());

        visitor.postVisitApply(result);

        return result;
    }
}

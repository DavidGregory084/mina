/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

import java.util.List;

public record LambdaNode<A> (Meta<A> meta, List<ParamNode<A>> params, ExprNode<A> body)
        implements ExprNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        params.forEach(param -> param.accept(visitor));
        body.accept(visitor);
        visitor.visitLambda(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitLambda(this);

        var result = visitor.visitLambda(
                meta(),
                params().stream().map(param -> param.accept(visitor)).toList(),
                visitor.visitExpr(body()));

        visitor.postVisitLambda(this);

        return result;
    }

    @Override
    public <B> LambdaNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitLambda(this);

        var result = visitor.visitLambda(
                meta(),
                params().stream().map(param -> param.accept(visitor)).toList(),
                visitor.visitExpr(body()));

        visitor.postVisitLambda(result);

        return result;
    }
}

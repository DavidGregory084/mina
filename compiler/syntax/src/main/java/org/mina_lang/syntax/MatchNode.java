/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

import java.util.List;

public record MatchNode<A> (Meta<A> meta, ExprNode<A> scrutinee, List<CaseNode<A>> cases)
        implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        scrutinee.accept(visitor);
        cases.forEach(cse -> cse.accept(visitor));
        visitor.visitMatch(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitMatch(this);

        var result = visitor.visitMatch(
                meta(),
                visitor.visitExpr(scrutinee()),
                cases().stream().map(cse -> cse.accept(visitor)).toList());

        visitor.postVisitMatch(this);

        return result;
    }

    @Override
    public <B> MatchNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitMatch(this);

        var result = visitor.visitMatch(
                meta(),
                visitor.visitExpr(scrutinee()),
                cases().stream().map(cse -> cse.accept(visitor)).toList());

        visitor.postVisitMatch(result);

        return result;
    }
}

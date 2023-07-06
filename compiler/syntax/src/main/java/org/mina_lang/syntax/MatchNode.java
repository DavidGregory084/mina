/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public record MatchNode<A> (Meta<A> meta, ExprNode<A> scrutinee, ImmutableList<CaseNode<A>> cases)
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
                cases().collect(cse -> cse.accept(visitor)));

        visitor.postVisitMatch(this);

        return result;
    }

    @Override
    public <B> MatchNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitMatch(this);

        var result = visitor.visitMatch(
                meta(),
                visitor.visitExpr(scrutinee()),
                cases().collect(cse -> cse.accept(visitor)));

        visitor.postVisitMatch(result);

        return result;
    }
}

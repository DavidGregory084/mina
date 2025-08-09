/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

import java.util.List;
import java.util.Optional;

public record BlockNode<A> (Meta<A> meta, List<LetNode<A>> declarations, Optional<ExprNode<A>> result)
        implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        declarations.forEach(decl -> decl.accept(visitor));
        result.ifPresent(visitor::visitExpr);
        visitor.visitBlock(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitBlock(this);

        var result = visitor.visitBlock(
                meta(),
                declarations().stream().map(let -> let.accept(visitor)).toList(),
                result().map(visitor::visitExpr));

        visitor.postVisitBlock(this);

        return result;
    }

    @Override
    public <B> BlockNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitBlock(this);

        var result = visitor.visitBlock(
                meta(),
                declarations().stream().map(let -> let.accept(visitor)).toList(),
                result().map(visitor::visitExpr));

        visitor.postVisitBlock(result);

        return result;
    }
}

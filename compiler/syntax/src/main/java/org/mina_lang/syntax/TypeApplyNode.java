/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

import java.util.List;

public record TypeApplyNode<A>(Meta<A> meta, TypeNode<A> type, List<TypeNode<A>> args)
        implements TypeNode<A> {

    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        type.accept(visitor);
        args.forEach(arg -> arg.accept(visitor));
        visitor.visitTypeApply(this);
    }

    @Override
    public <B> B accept(TypeNodeFolder<A, B> visitor) {
        visitor.preVisitTypeApply(this);

        var result = visitor.visitTypeApply(
                meta(),
                visitor.visitType(type()),
                args().stream().map(visitor::visitType).toList());

        visitor.postVisitTypeApply(result);

        return result;
    }

    @Override
    public <B> TypeApplyNode<B> accept(TypeNodeTransformer<A, B> visitor) {
        visitor.preVisitTypeApply(this);

        var result = visitor.visitTypeApply(
                meta(),
                visitor.visitType(type()),
                args().stream().map(visitor::visitType).toList());

        visitor.postVisitTypeApply(result);

        return result;
    }
}

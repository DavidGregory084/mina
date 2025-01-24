/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

public record UnaryOpNode<A>(Meta<A> meta, UnaryOp operator, ExprNode<A> operand) implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        operand.accept(visitor);
        visitor.visitUnaryOpNode(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitUnaryOp(this);

        var result = visitor.visitUnaryOp(
            meta(),
            operator(),
            visitor.visitExpr(operand()));

        visitor.postVisitUnaryOp(this);

        return result;
    }

    @Override
    public <B> UnaryOpNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitUnaryOp(this);

        var result = visitor.visitUnaryOp(
            meta(),
            operator(),
            visitor.visitExpr(operand()));

        visitor.postVisitUnaryOp(this);

        return result;
    }
}

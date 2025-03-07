/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;
import org.mina_lang.common.operators.BinaryOp;

public record BinaryOpNode<A>(Meta<A> meta, ExprNode<A> leftOperand, BinaryOp operator, ExprNode<A> rightOperand) implements ExprNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        leftOperand.accept(visitor);
        rightOperand.accept(visitor);
        visitor.visitBinaryOpNode(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitBinaryOp(this);

        var result = visitor.visitBinaryOp(
            meta(),
            visitor.visitExpr(leftOperand()),
            operator(),
            visitor.visitExpr(rightOperand()));

        visitor.postVisitBinaryOp(this);

        return result;
    }

    @Override
    public <B> BinaryOpNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitBinaryOp(this);

        var result = visitor.visitBinaryOp(
            meta(),
            visitor.visitExpr(leftOperand()),
            operator(),
            visitor.visitExpr(rightOperand()));

        visitor.postVisitBinaryOp(this);

        return result;
    }
}

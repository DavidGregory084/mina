/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ForAllVarName;

public record ForAllVarNode<A> (Meta<A> meta, String name) implements TypeVarNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        visitor.visitForAllVar(this);
    }

    @Override
    public <B> B accept(TypeNodeFolder<A, B> visitor) {
        visitor.preVisitForAllVar(this);
        var result = visitor.visitForAllVar(meta(), name());
        visitor.postVisitForAllVar(result);
        return result;
    }

    @Override
    public <B> ForAllVarNode<B> accept(TypeNodeTransformer<A, B> visitor) {
        visitor.preVisitForAllVar(this);
        var result = visitor.visitForAllVar(meta(), name());
        visitor.postVisitForAllVar(result);
        return result;
    }

    @Override
    public ForAllVarName getName() {
        return new ForAllVarName(name());
    }
}

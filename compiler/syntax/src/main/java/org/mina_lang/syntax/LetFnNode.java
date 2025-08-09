/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;
import org.mina_lang.common.names.LetName;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.common.names.QualifiedName;

import java.util.List;
import java.util.Optional;

public record LetFnNode<A> (Meta<A> meta, String name, List<TypeVarNode<A>> typeParams,
        List<ParamNode<A>> valueParams, Optional<TypeNode<A>> returnType, ExprNode<A> expr)
        implements DeclarationNode<A> {
    @Override
    public void accept(SyntaxNodeVisitor visitor) {
        typeParams.forEach(tyParam -> tyParam.accept(visitor));
        valueParams.forEach(param -> param.accept(visitor));
        returnType.ifPresent(returnTy -> returnTy.accept(visitor));
        expr.accept(visitor);
        visitor.visitLetFn(this);
    }

    @Override
    public <B> B accept(MetaNodeFolder<A, B> visitor) {
        visitor.preVisitLetFn(this);

        var result = visitor.visitLetFn(
                meta(),
                name(),
                typeParams().stream().map(visitor::visitTypeVar).toList(),
                valueParams().stream().map(param -> param.accept(visitor)).toList(),
                returnType().map(visitor::visitType),
                visitor.visitExpr(expr()));

        visitor.postVisitLetFn(this);

        return result;
    }

    @Override
    public <B> LetFnNode<B> accept(MetaNodeTransformer<A, B> visitor) {
        visitor.preVisitLetFn(this);

        var result = visitor.visitLetFn(
                meta(),
                name(),
                typeParams().stream().map(visitor::visitTypeVar).toList(),
                valueParams().stream().map(param -> param.accept(visitor)).toList(),
                returnType().map(visitor::visitType),
                visitor.visitExpr(expr()));

        visitor.postVisitLetFn(result);

        return result;
    }

    public LetName getName(NamespaceName namespace) {
        return new LetName(new QualifiedName(namespace, name));
    }
}

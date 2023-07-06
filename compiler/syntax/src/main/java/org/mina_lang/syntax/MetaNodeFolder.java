/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

import java.util.Optional;

public interface MetaNodeFolder<A, B> extends DataNodeFolder<A, B>, PatternNodeFolder<A, B> {

    // Namespaces
    default void preVisitNamespace(NamespaceNode<A> namespace) {}

    B visitNamespace(Meta<A> meta, NamespaceIdNode id, ImmutableList<ImportNode> imports,
            ImmutableList<ImmutableList<B>> declarationGroups);

    default void postVisitNamespace(NamespaceNode<A> namespace) {}

    // Declarations
    default B visitDeclaration(DeclarationNode<A> decl) {
        return decl.accept(this);
    }

    default void preVisitLet(LetNode<A> let) {}

    B visitLet(Meta<A> meta, String name, Optional<B> type, B expr);

    default void postVisitLet(LetNode<A> let) {}


    default void preVisitLetFn(LetFnNode<A> letFn) {}

    B visitLetFn(Meta<A> meta, String name, ImmutableList<B> typeParams, ImmutableList<B> valueParams,
            Optional<B> returnType, B expr);

    default void postVisitLetFn(LetFnNode<A> letFn) {}


    default void preVisitParam(ParamNode<A> param) {}

    B visitParam(Meta<A> param, String name, Optional<B> typeAnnotation);

    default void postVisitParam(ParamNode<A> param) {}

    // Expressions
    default B visitExpr(ExprNode<A> expr) {
        return expr.accept(this);
    }

    default void preVisitBlock(BlockNode<A> block) {}

    B visitBlock(Meta<A> meta, ImmutableList<B> declarations, Optional<B> result);

    default void postVisitBlock(BlockNode<A> block) {}


    default void preVisitIf(IfNode<A> ifExpr) {}

    B visitIf(Meta<A> meta, B condition, B consequent, B alternative);

    default void postVisitIf(IfNode<A> ifExpr) {}


    default void preVisitLambda(LambdaNode<A> lambda) {}

    B visitLambda(Meta<A> meta, ImmutableList<B> params, B body);

    default void postVisitLambda(LambdaNode<A> lambda) {}


    default void preVisitMatch(MatchNode<A> match) {}

    B visitMatch(Meta<A> meta, B scrutinee, ImmutableList<B> cases);

    default void postVisitMatch(MatchNode<A> match) {}


    default void preVisitApply(ApplyNode<A> apply) {}

    B visitApply(Meta<A> meta, B expr, ImmutableList<B> args);

    default void postVisitApply(ApplyNode<A> apply) {}


    default void preVisitReference(ReferenceNode<A> ref) {}

    B visitReference(Meta<A> meta, QualifiedIdNode id);

    default void postVisitReference(ReferenceNode<A> ref) {}

    // Cases and patterns
    default void preVisitCase(CaseNode<A> cse) {}

    B visitCase(Meta<A> meta, B pattern, B consequent);

    default void postVisitCase(CaseNode<A> cse) {}
}

package org.mina_lang.syntax;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public interface MetaNodeTransformer<A, B> extends DataNodeTransformer<A, B>, PatternNodeTransformer<A, B> {

    // Namespaces
    default void preVisitNamespace(NamespaceNode<A> namespace) {}

    NamespaceNode<B> visitNamespace(Meta<A> meta, NamespaceIdNode id, ImmutableList<ImportNode> imports,
            ImmutableList<DeclarationNode<B>> declarations);

    default void postVisitNamespace(NamespaceNode<B> namespace) {}

    // Declarations
    default DeclarationNode<B> visitDeclaration(DeclarationNode<A> decl) {
        return decl.accept(this);
    }

    default void preVisitLet(LetNode<A> let) {}

    LetNode<B> visitLet(Meta<A> meta, String name, Optional<TypeNode<B>> type, ExprNode<B> expr);

    default void postVisitLet(LetNode<B> let) {}


    default void preVisitLetFn(LetFnNode<A> letFn) {}

    LetFnNode<B> visitLetFn(Meta<A> meta, String name, ImmutableList<TypeVarNode<B>> typeParams,
            ImmutableList<ParamNode<B>> valueParams, Optional<TypeNode<B>> returnType, ExprNode<B> expr);

    default void postVisitLetFn(LetFnNode<B> letFn) {}


    default void preVisitParam(ParamNode<A> param) {}

    ParamNode<B> visitParam(Meta<A> param, String name, Optional<TypeNode<B>> typeAnnotation);

    default void postVisitParam(ParamNode<B> param) {}

    // Expressions
    default ExprNode<B> visitExpr(ExprNode<A> expr) {
        return expr.accept(this);
    }

    default void preVisitBlock(BlockNode<A> block) {}

    BlockNode<B> visitBlock(Meta<A> meta, ImmutableList<LetNode<B>> declarations, Optional<ExprNode<B>> result);

    default void postVisitBlock(BlockNode<B> block) {}


    default void preVisitIf(IfNode<A> ifExpr) {}

    IfNode<B> visitIf(Meta<A> meta, ExprNode<B> condition, ExprNode<B> consequent, ExprNode<B> alternative);

    default void postVisitIf(IfNode<B> ifExpr) {}


    default void preVisitLambda(LambdaNode<A> lambda) {}

    LambdaNode<B> visitLambda(Meta<A> meta, ImmutableList<ParamNode<B>> params, ExprNode<B> body);

    default void postVisitLambda(LambdaNode<B> lambda) {}


    default void preVisitMatch(MatchNode<A> match) {}

    MatchNode<B> visitMatch(Meta<A> meta, ExprNode<B> scrutinee, ImmutableList<CaseNode<B>> cases);

    default void postVisitMatch(MatchNode<B> match) {}


    default void preVisitApply(ApplyNode<A> apply) {}

    ApplyNode<B> visitApply(Meta<A> meta, ExprNode<B> expr, ImmutableList<ExprNode<B>> args);

    default void postVisitApply(ApplyNode<B> apply) {}


    default void preVisitReference(ReferenceNode<A> ref) {}

    ReferenceNode<B> visitReference(Meta<A> meta, QualifiedIdNode id);

    default void postVisitReference(ReferenceNode<B> ref) {}


    // Cases and patterns
    default void preVisitCase(CaseNode<A> cse) {}

    CaseNode<B> visitCase(Meta<A> meta, PatternNode<B> pattern, ExprNode<B> consequent);

    default void postVisitCase(CaseNode<B> cse) {}
}

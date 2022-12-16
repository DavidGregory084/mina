package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public interface TypeNodeTransformer<A, B> {

    default TypeNode<B> visitType(TypeNode<A> typ) {
        return typ.accept(this);
    }

    default void preVisitTypeLambda(TypeLambdaNode<A> tyLam) {}

    TypeLambdaNode<B> visitTypeLambda(Meta<A> meta, ImmutableList<TypeVarNode<B>> args, TypeNode<B> body);

    default void postVisitTypeLambda(TypeLambdaNode<B> tyLam) {}


    default void preVisitFunType(FunTypeNode<A> funTyp) {}

    FunTypeNode<B> visitFunType(Meta<A> meta, ImmutableList<TypeNode<B>> argTypes, TypeNode<B> returnType);

    default void postVisitFunType(FunTypeNode<B> funTyp) {}


    default void preVisitTypeApply(TypeApplyNode<A> tyApp) {}

    TypeApplyNode<B> visitTypeApply(Meta<A> meta, TypeNode<B> type, ImmutableList<TypeNode<B>> args);

    default void postVisitTypeApply(TypeApplyNode<B> tyApp) {}


    default void preVisitTypeReference(TypeReferenceNode<A> tyRef) {}

    TypeReferenceNode<B> visitTypeReference(Meta<A> meta, QualifiedIdNode id);

    default void postVisitTypeReference(TypeReferenceNode<B> tyRef) {}


    default TypeVarNode<B> visitTypeVar(TypeVarNode<A> tyVar) {
        return tyVar.accept(this);
    }


    default void preVisitForAllVar(ForAllVarNode<A> forAllVar) {}

    ForAllVarNode<B> visitForAllVar(Meta<A> meta, String name);

    default void postVisitForAllVar(ForAllVarNode<B> forAllVar) {}


    default void preVisitExistsVar(ExistsVarNode<A> existsVar) {}

    ExistsVarNode<B> visitExistsVar(Meta<A> meta, String name);

    default void postVisitExistsVar(ExistsVarNode<B> existsVar) {}
}

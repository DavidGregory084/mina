package org.mina_lang.syntax;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Meta;

public interface DataNodeTransformer<A, B> extends TypeNodeTransformer<A, B> {

    default void preVisitData(DataNode<A> data) {}

    DataNode<B> visitData(Meta<A> meta, String name, ImmutableList<TypeVarNode<B>> typeParams, ImmutableList<ConstructorNode<B>> constructors);

    default void postVisitData(DataNode<B> data) {}


    default void preVisitConstructor(ConstructorNode<A> constr) {}

    ConstructorNode<B> visitConstructor(Meta<A> meta, String name, ImmutableList<ConstructorParamNode<B>> params, Optional<TypeNode<B>> type);

    default void postVisitConstructor(ConstructorNode<B> constr) {}


    default void preVisitConstructorParam(ConstructorParamNode<A> constrParam) {}

    ConstructorParamNode<B> visitConstructorParam(Meta<A> meta, String name, TypeNode<B> typeAnnotation);

    default void postVisitConstructorParam(ConstructorParamNode<B> constrParam) {}
}

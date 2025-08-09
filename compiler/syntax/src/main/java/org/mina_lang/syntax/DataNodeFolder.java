/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import org.mina_lang.common.Meta;

import java.util.List;
import java.util.Optional;

public interface DataNodeFolder<A, B> extends TypeNodeFolder<A, B> {

    default void preVisitData(DataNode<A> data) {}

    B visitData(Meta<A> meta, String name, List<B> typeParams, List<B> constructors);

    default void postVisitData(B data) {}


    default void preVisitConstructor(ConstructorNode<A> constr) {}

    B visitConstructor(Meta<A> meta, String name, List<B> params, Optional<B> type);

    default void postVisitConstructor(B constr) {}


    default void preVisitConstructorParam(ConstructorParamNode<A> constrParam) {}

    B visitConstructorParam(Meta<A> meta, String name, B typeAnnotation);

    default void postVisitConstructorParam(B constrParam) {}
}

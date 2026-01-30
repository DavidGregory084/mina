/*
 * SPDX-FileCopyrightText:  © 2026 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.optimiser.constants;

import net.jqwik.api.*;
import org.eclipse.collections.impl.factory.Lists;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.DataName;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.common.names.QualifiedName;
import org.mina_lang.common.types.Type;
import org.mina_lang.common.types.TypeConstructor;
import org.mina_lang.common.types.TypeKind;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ResultTest {
    NamespaceName namespaceName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Constants");

    @Property
    void unknownIsLeast(@ForAll("results") Result result) {
        assertThat(Result.leastUpperBound(result, Unknown.VALUE), equalTo(result));
    }

    @Property
    void nonConstantIsGreatest(@ForAll("results") Result result) {
        assertThat(Result.leastUpperBound(result, NonConstant.VALUE), equalTo(NonConstant.VALUE));
    }

    @Property
    void unequalConstantsAreIncomparable(@ForAll("constants") Result left, @ForAll("constants") Result right) {
        if (left.equals(right)) {
            assertThat(
                Result.leastUpperBound(left, right),
                equalTo(left));
        } else {
            assertThat(
                Result.leastUpperBound(left, right),
                equalTo(NonConstant.VALUE));
        }
    }

    @Property
    void knownConstructorGreaterThanConstantConstructor(@ForAll("constructorNames") ConstructorName constrName) {
        var constrType = Type.function(new TypeConstructor(constrName.name(), TypeKind.INSTANCE));
        var knownConstructor = new KnownConstructor(constrName);
        var constantConstructor = new ConstantConstructor(constrName, constrType);
        assertThat(Result.leastUpperBound(knownConstructor, constantConstructor), equalTo(knownConstructor));
        assertThat(Result.leastUpperBound(constantConstructor, knownConstructor), equalTo(knownConstructor));
    }

    @Property
    void knownConstructorForSameConstructorsEqual(@ForAll("constructorNames") ConstructorName constrName) {
        var known = new KnownConstructor(constrName);
        assertThat(Result.leastUpperBound(known, known), equalTo(known));
    }

    @Property
    void constantConstructorForSameConstructorsEqual(@ForAll("constructorNames") ConstructorName constrName) {
        var constrType = Type.function(new TypeConstructor(constrName.name(), TypeKind.INSTANCE));
        var constant = new ConstantConstructor(constrName, constrType);
        assertThat(Result.leastUpperBound(constant, constant), equalTo(constant));
    }

    @Property
    void constructorResultsForDifferentConstructorsIncomparable(
        @ForAll("constructorNames") ConstructorName leftConstr,
        @ForAll("constructorNames") ConstructorName rightConstr
    ) {
        var leftKnown = new KnownConstructor(leftConstr);
        var rightKnown = new KnownConstructor(rightConstr);
        var leftConstrType = Type.function(new TypeConstructor(leftConstr.name(), TypeKind.INSTANCE));
        var rightConstrType = Type.function(new TypeConstructor(rightConstr.name(), TypeKind.INSTANCE));
        var leftConstant = new ConstantConstructor(leftConstr, leftConstrType);
        var rightConstant = new ConstantConstructor(rightConstr, rightConstrType);
        if (!leftConstr.equals(rightConstr)) {
            assertThat(Result.leastUpperBound(leftKnown, rightKnown), equalTo(NonConstant.VALUE));
            assertThat(Result.leastUpperBound(leftConstant, rightKnown), equalTo(NonConstant.VALUE));
            assertThat(Result.leastUpperBound(leftKnown, rightConstant), equalTo(NonConstant.VALUE));
            assertThat(Result.leastUpperBound(leftConstant, rightConstant), equalTo(NonConstant.VALUE));
        }
    }

    @Provide
    Arbitrary<Result> results(@ForAll("constants") Result constant) {
        return Arbitraries.oneOf(
            Arbitraries.just(Unknown.VALUE),
            Arbitraries.just(NonConstant.VALUE),
            Arbitraries.just(constant)
        );
    }

    @Provide
    Arbitrary<ConstructorName> constructorNames() {
        return Arbitraries.strings()
            .ofMinLength(3).ofMaxLength(5).tuple2()
            .map(tuple -> {
                var dataName = new DataName(new QualifiedName(namespaceName, tuple.get1()));
                return new ConstructorName(dataName, new QualifiedName(namespaceName, tuple.get2()));
            });
    }

    @Provide
    Arbitrary<Result> constants() {
        return Arbitraries.oneOf(
            Arbitraries.chars().map(chr -> new Constant(new org.mina_lang.ina.Char(chr))),
            Arbitraries.doubles().map(dbl -> new Constant(new org.mina_lang.ina.Double(dbl))),
            Arbitraries.floats().map(flt -> new Constant(new org.mina_lang.ina.Float(flt))),
            Arbitraries.integers().map(intgr -> new Constant(new org.mina_lang.ina.Int(intgr))),
            Arbitraries.longs().map(lng -> new Constant(new org.mina_lang.ina.Long(lng))),
            Arbitraries.strings().map(string -> new Constant(new org.mina_lang.ina.String(string))),
            constructorNames().map(KnownConstructor::new),
            constructorNames().map(name -> new ConstantConstructor(name, Type.function(new TypeConstructor(name.name(), TypeKind.INSTANCE))))
        );
    }
}

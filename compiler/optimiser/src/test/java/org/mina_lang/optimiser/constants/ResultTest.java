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

    @Provide
    Arbitrary<Result> results(@ForAll("constants") Result constant) {
        return Arbitraries.oneOf(
            Arbitraries.just(Unknown.VALUE),
            Arbitraries.just(NonConstant.VALUE),
            Arbitraries.just(constant)
        );
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
            Arbitraries.strings().ofMinLength(3).ofMaxLength(5).tuple2()
                .map(tuple -> {
                    var dataName = new DataName(new QualifiedName(namespaceName, tuple.get1()));
                    var constrName = new ConstructorName(dataName, new QualifiedName(namespaceName, tuple.get2()));
                    return new KnownConstructor(constrName);
                }),
            Arbitraries.strings().ofMinLength(3).ofMaxLength(5).tuple2()
                .map(tuple -> {
                    var dataName = new DataName(new QualifiedName(namespaceName, tuple.get1()));
                    var constrName = new ConstructorName(dataName, new QualifiedName(namespaceName, tuple.get2()));
                    return new ConstantConstructor(constrName);
                })
        );
    }
}

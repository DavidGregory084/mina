package org.mina_lang.optimiser.constants;

import net.jqwik.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ResultTest {
    @Property
    void unassignedIsLeast(@ForAll("results") Result result) {
        assertThat(Result.leastUpperBound(result, Unassigned.VALUE), equalTo(result));
    }

    @Property
    void nonConstantIsGreatest(@ForAll("results") Result result) {
        assertThat(Result.leastUpperBound(result, NonConstant.VALUE), equalTo(NonConstant.VALUE));
    }

    @Property
    void unequalConstantsAreIncomparable(@ForAll("constantValues") Result left, @ForAll("constantValues") Result right) {
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
    Arbitrary<Result> results(@ForAll("constantValues") Result constant) {
        return Arbitraries.oneOf(
            Arbitraries.just(Unassigned.VALUE),
            Arbitraries.just(NonConstant.VALUE),
            Arbitraries.just(constant)
        );
    }

    @Provide
    Arbitrary<Result> constantValues() {
        return Arbitraries.oneOf(
            Arbitraries.chars().map(chr -> new Constant(new org.mina_lang.ina.Char(chr))),
            Arbitraries.doubles().map(dbl -> new Constant(new org.mina_lang.ina.Double(dbl))),
            Arbitraries.floats().map(flt -> new Constant(new org.mina_lang.ina.Float(flt))),
            Arbitraries.integers().map(intgr -> new Constant(new org.mina_lang.ina.Int(intgr))),
            Arbitraries.longs().map(lng -> new Constant(new org.mina_lang.ina.Long(lng))),
            Arbitraries.strings().map(string -> new Constant(new org.mina_lang.ina.String(string)))
        );
    }
}

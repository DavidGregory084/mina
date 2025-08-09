/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

import it.unimi.dsi.fastutil.Pair;
import net.jqwik.api.*;

import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class UnionFindTest {

    BinaryOperator<Kind> chooseKindConstant = (l, r) -> {
        if (l instanceof UnsolvedKind) {
            if (r instanceof UnsolvedKind) {
                return l;
            } else {
                return r;
            }
        } else {
            return l;
        }
    };

    @Property
    void elementsReturnsAllInputs(@ForAll Set<@From("kinds") Kind> kinds) {
        var unionFind = UnionFind.ofAll(chooseKindConstant, kinds);
        assertThat(unionFind.elements(), containsInAnyOrder(kinds.toArray()));
        for (var kind : kinds) {
            assertThat(unionFind.find(kind), is(equalTo(kind)));
        }
    }

    @Property
    void findReturnsSelfBeforeUnion(@ForAll("kinds") Kind kind) {
        var unionFind = UnionFind.of(chooseKindConstant, kind);
        assertThat(unionFind.find(kind), is(equalTo(kind)));
    }

    @Property
    void findPicksTypeKindOverUnsolved(
            @ForAll("distinctUnsolved") Pair<UnsolvedKind, UnsolvedKind> unsolved) {
        var unsolved1 = unsolved.first();
        var unsolved2 = unsolved.second();

        var unionFind = UnionFind.of(chooseKindConstant, unsolved1, unsolved2, TypeKind.INSTANCE);

        assertThat(unionFind.find(unsolved1), is(equalTo(unsolved1)));
        assertThat(unionFind.find(unsolved2), is(equalTo(unsolved2)));
        assertThat(unionFind.find(TypeKind.INSTANCE), is(equalTo(TypeKind.INSTANCE)));

        unionFind.union(unsolved1, TypeKind.INSTANCE);

        assertThat(unionFind.find(unsolved1), is(equalTo(TypeKind.INSTANCE)));
        assertThat(unionFind.find(unsolved2), is(equalTo(unsolved2)));
        assertThat(unionFind.find(TypeKind.INSTANCE), is(equalTo(TypeKind.INSTANCE)));

        unionFind.union(unsolved2, TypeKind.INSTANCE);

        assertThat(unionFind.find(unsolved1), is(equalTo(TypeKind.INSTANCE)));
        assertThat(unionFind.find(unsolved2), is(equalTo(TypeKind.INSTANCE)));
        assertThat(unionFind.find(TypeKind.INSTANCE), is(equalTo(TypeKind.INSTANCE)));
    }

    @Property
    void findPicksHigherKindOverUnsolved(
            @ForAll("distinctUnsolved") Pair<UnsolvedKind, UnsolvedKind> unsolved,
            @ForAll List<@From("kinds") Kind> kindArgs) {
        var unsolved1 = unsolved.first();
        var unsolved2 = unsolved.second();
        var higher = new HigherKind(kindArgs, TypeKind.INSTANCE);

        var unionFind = UnionFind.of(chooseKindConstant, unsolved1, unsolved2, higher);

        assertThat(unionFind.find(unsolved1), is(equalTo(unsolved1)));
        assertThat(unionFind.find(unsolved2), is(equalTo(unsolved2)));
        assertThat(unionFind.find(higher), is(equalTo(higher)));

        unionFind.union(unsolved1, higher);

        assertThat(unionFind.find(unsolved1), is(equalTo(higher)));
        assertThat(unionFind.find(unsolved2), is(equalTo(unsolved2)));
        assertThat(unionFind.find(higher), is(equalTo(higher)));

        unionFind.union(unsolved2, higher);

        assertThat(unionFind.find(unsolved1), is(equalTo(higher)));
        assertThat(unionFind.find(unsolved2), is(equalTo(higher)));
        assertThat(unionFind.find(higher), is(equalTo(higher)));
    }

    @Provide
    Arbitrary<Pair<UnsolvedKind, UnsolvedKind>> distinctUnsolved() {
        return Arbitraries.integers()
                .between(0, 99)
                .map(UnsolvedKind::new)
                .tuple2()
                .filter(tuple -> !tuple.get1().equals(tuple.get2()))
                .map(tuple -> Pair.of(tuple.get1(), tuple.get2()));
    }

    @Provide
    Arbitrary<Kind> kinds() {
        return Arbitraries.lazyOf(
                () -> Arbitraries.just(TypeKind.INSTANCE),
                () -> Arbitraries.integers().map(UnsolvedKind::new),
                () -> kinds().list()
                        .ofMaxSize(5)
                        .map(kindArgs -> new HigherKind(kindArgs, TypeKind.INSTANCE)));
    }
}

/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

import net.jqwik.api.*;
import org.eclipse.collections.api.tuple.Twin;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.tuple.Tuples;

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
            @ForAll("distinctUnsolved") Twin<UnsolvedKind> unsolved) {
        var unsolved1 = unsolved.getOne();
        var unsolved2 = unsolved.getTwo();

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
            @ForAll("distinctUnsolved") Twin<UnsolvedKind> unsolved,
            @ForAll List<@From("kinds") Kind> kindArgs) {
        var unsolved1 = unsolved.getOne();
        var unsolved2 = unsolved.getTwo();
        var higher = new HigherKind(Lists.immutable.ofAll(kindArgs), TypeKind.INSTANCE);

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
    Arbitrary<Twin<UnsolvedKind>> distinctUnsolved() {
        return Arbitraries.integers()
                .between(0, 99)
                .map(UnsolvedKind::new)
                .tuple2()
                .filter(tuple -> !tuple.get1().equals(tuple.get2()))
                .map(tuple -> Tuples.twin(tuple.get1(), tuple.get2()));
    }

    @Provide
    Arbitrary<Kind> kinds() {
        return Arbitraries.lazyOf(
                () -> Arbitraries.just(TypeKind.INSTANCE),
                () -> Arbitraries.integers().map(UnsolvedKind::new),
                () -> kinds().list()
                        .ofMaxSize(5)
                        .map(kindArgs -> new HigherKind(Lists.immutable.ofAll(kindArgs), TypeKind.INSTANCE)));
    }
}

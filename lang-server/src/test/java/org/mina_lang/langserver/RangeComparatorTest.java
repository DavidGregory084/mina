/*
 * SPDX-FileCopyrightText:  © 2022 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.langserver;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.Comparator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class RangeComparatorTest {
    Comparator<Range> comparator = new RangeComparator();

    @Property
    void laterStartComparesLesser(
            @ForAll @IntRange(min = 0, max = 10) int firstLine,
            @ForAll @IntRange(min = 0, max = 10) int firstChar,
            @ForAll @IntRange(min = 0, max = 10) int secondLine,
            @ForAll @IntRange(min = 0, max = 10) int secondChar) {
        var first = new Range(new Position(firstLine, firstChar), new Position(10, 10));
        var second = new Range(new Position(secondLine, secondChar), new Position(10, 10));
        if (firstLine > secondLine) {
            assertThat(comparator.compare(first, second), is(equalTo(-1)));
        } else if (firstLine < secondLine) {
            assertThat(comparator.compare(first, second), is(equalTo(1)));
        } else if (firstChar > secondChar) {
            assertThat(comparator.compare(first, second), is(equalTo(-1)));
        } else if (firstChar < secondChar) {
            assertThat(comparator.compare(first, second), is(equalTo(1)));
        } else {
            assertThat(comparator.compare(first, second), is(equalTo(0)));
        }
    }

    @Property
    void earlierEndComparesLesser(
            @ForAll @IntRange(min = 0, max = 10) int firstLine,
            @ForAll @IntRange(min = 0, max = 10) int firstChar,
            @ForAll @IntRange(min = 0, max = 10) int secondLine,
            @ForAll @IntRange(min = 0, max = 10) int secondChar) {
        var first = new Range(new Position(0, 0), new Position(firstLine, firstChar));
        var second = new Range(new Position(0, 0), new Position(secondLine, secondChar));
        if (firstLine > secondLine) {
            assertThat(comparator.compare(first, second), is(equalTo(1)));
        } else if (firstLine < secondLine) {
            assertThat(comparator.compare(first, second), is(equalTo(-1)));
        } else if (firstChar > secondChar) {
            assertThat(comparator.compare(first, second), is(equalTo(1)));
        } else if (firstChar < secondChar) {
            assertThat(comparator.compare(first, second), is(equalTo(-1)));
        } else {
            assertThat(comparator.compare(first, second), is(equalTo(0)));
        }
    }
}

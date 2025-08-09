/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class KindPrinterTest {
    KindPrinter printer = new KindPrinter();

    @Test
    void testTypeKindPrinting() {
        var doc = TypeKind.INSTANCE.accept(printer);
        assertThat(doc.render(80), is(equalTo("*")));
    }

    @Test
    void testUnsolvedKindPrinting() {
        // Starts with A1
        var typeVar0 = new UnsolvedKind(0);
        var doc0 = typeVar0.accept(printer);
        assertThat(doc0.render(80), is(equalTo("?A1")));
        // Proceeds through alphabetic characters
        var typeVar1 = new UnsolvedKind(1);
        var doc1 = typeVar1.accept(printer);
        assertThat(doc1.render(80), is(equalTo("?B1")));
        // After Z1, increments suffix
        var typeVar26 = new UnsolvedKind(26);
        var doc26 = typeVar26.accept(printer);
        assertThat(doc26.render(80), is(equalTo("?A2")));
    }

    @Test
    void testHigherKindPrinting() {
        var unaryKind = new HigherKind(List.of(TypeKind.INSTANCE), TypeKind.INSTANCE);
        assertThat(unaryKind.accept(printer).render(80), is(equalTo("* => *")));
        assertThat(unaryKind.accept(printer).render(4), is(equalTo(String.format("* =>%n  *"))));

        var binaryKind = new HigherKind(List.of(TypeKind.INSTANCE, TypeKind.INSTANCE), TypeKind.INSTANCE);
        assertThat(binaryKind.accept(printer).render(80), is(equalTo("[*, *] => *")));
        assertThat(binaryKind.accept(printer).render(10), is(equalTo(String.format("[*, *] =>%n  *"))));
        assertThat(binaryKind.accept(printer).render(4), is(equalTo(String.format("[%n  *,%n  *%n] =>%n  *"))));

        var higherKind = new HigherKind(
                List.of(new HigherKind(List.of(TypeKind.INSTANCE), TypeKind.INSTANCE)),
                TypeKind.INSTANCE);

        assertThat(higherKind.accept(printer).render(80), is(equalTo("[* => *] => *")));
        assertThat(higherKind.accept(printer).render(10), is(equalTo(String.format("[* => *] =>%n  *"))));
        assertThat(higherKind.accept(printer).render(4), is(equalTo(String.format("[%n  * =>%n    *%n] =>%n  *"))));
    }
}

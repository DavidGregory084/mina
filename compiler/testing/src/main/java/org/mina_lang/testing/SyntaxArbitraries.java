/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.testing;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.Nameless;
import org.mina_lang.common.types.Type;
import org.mina_lang.syntax.*;

public class SyntaxArbitraries {
    private SyntaxArbitraries() {
    }

    private static final Arbitrary<Boolean> booleanArbitrary = Arbitraries.of(true, false);

    public static Arbitrary<BooleanNode<Attributes>> booleanNode = booleanArbitrary.map(bool -> {
        return SyntaxNodes.boolNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.BOOLEAN)), bool);
    });

    public static Arbitrary<CharNode<Attributes>> charNode = Arbitraries.chars().map(character -> {
        return SyntaxNodes.charNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.CHAR)), character);
    });

    public static Arbitrary<StringNode<Attributes>> stringNode = Arbitraries.strings().ascii().ofMaxLength(50).map(string -> {
        return SyntaxNodes.stringNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.STRING)), string);
    });

    public static Arbitrary<IntNode<Attributes>> intNode = Arbitraries.integers().greaterOrEqual(0).map(i -> {
        return SyntaxNodes.intNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.INT)), i);
    });

    public static Arbitrary<LongNode<Attributes>> longNode = Arbitraries.longs().greaterOrEqual(0L).map(l -> {
        return SyntaxNodes.longNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.LONG)), l);
    });

    public static Arbitrary<FloatNode<Attributes>> floatNode = Arbitraries.floats().greaterOrEqual(0F).map(f -> {
        return SyntaxNodes.floatNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.FLOAT)), f);
    });

    public static Arbitrary<DoubleNode<Attributes>> doubleNode = Arbitraries.doubles().greaterOrEqual(0D).map(d -> {
        return SyntaxNodes.doubleNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.DOUBLE)), d);
    });

    Arbitrary<LiteralNode<Attributes>> literalNode = Arbitraries.oneOf(
        booleanNode, charNode, stringNode, intNode, longNode, floatNode, doubleNode);
}

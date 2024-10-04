/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.testing;

import com.ibm.icu.text.UnicodeSet;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.arbitraries.StringArbitrary;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.Nameless;
import org.mina_lang.common.types.Type;
import org.mina_lang.syntax.*;

public class SyntaxArbitraries {
    private SyntaxArbitraries() {
    }

    private static final UnicodeSet nameBeginChars = new UnicodeSet("[\\p{XID_Start}&\\p{Identifier_Status=Allowed}]");
    private static final Arbitrary<String> nameBeginArbitrary = unicodeStrings(nameBeginChars).ofLength(1);

    private static final UnicodeSet nameContinueChars = new UnicodeSet("[\\p{XID_Continue}&\\p{Identifier_Status=Allowed}]");
    private static final Arbitrary<String> nameContinueArbitrary = unicodeStrings(nameContinueChars).ofMaxLength(19);

    private static StringArbitrary unicodeStrings(UnicodeSet unicodeSet) {
        var strings = Arbitraries.strings();
        for (var range : unicodeSet.ranges()) {
            if (range.codepoint > Character.MAX_VALUE) { break; }
            char rangeStart = (char) range.codepoint;
            char rangeEnd = range.codepointEnd > Character.MAX_VALUE
                ? Character.MAX_VALUE
                : (char) range.codepointEnd;
            strings = strings.withCharRange(rangeStart, rangeEnd);
        }
        return strings;
    }

    private static final Arbitrary<String> nameArbitrary = Combinators
        .combine(nameBeginArbitrary, nameContinueArbitrary)
        .as((begin, cont) -> begin + cont);

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

    public static Arbitrary<LiteralNode<Attributes>> literalNode = Arbitraries.oneOf(
        booleanNode, charNode, stringNode, intNode, longNode, floatNode, doubleNode);

    public static Arbitrary<ExprNode<Attributes>> exprNode = Arbitraries.oneOf(
        literalNode);
}

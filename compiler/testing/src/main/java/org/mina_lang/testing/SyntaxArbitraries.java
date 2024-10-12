/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.testing;

import com.ibm.icu.text.UnicodeSet;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Tuple;
import net.jqwik.api.arbitraries.StringArbitrary;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.names.Named;
import org.mina_lang.common.types.Type;
import org.mina_lang.syntax.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class SyntaxArbitraries {
    private SyntaxArbitraries() {
    }

    private static Name getName(MetaNode<Attributes> node) {
        return node.meta().meta().name();
    }

    private static Type getType(MetaNode<Attributes> node) {
        return (Type) node.meta().meta().sort();
    }

    private static final Arbitrary<Character.UnicodeScript> scriptArbitrary = Arbitraries.forType(Character.UnicodeScript.class);

    private static final UnicodeSet nameBeginChars = new UnicodeSet("[[:XID_Start:]&[:Identifier_Status=Allowed:]]");
    private static final Arbitrary<String> nameBeginArbitrary = unicodeStrings(nameBeginChars).ofLength(1);

    private static final UnicodeSet nameContinueChars = new UnicodeSet("[[:XID_Continue:]&[:Identifier_Status=Allowed:]]");
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

    private static final Set<String> reservedWords = Set.of(
        "namespace",
        "import",
        "as",
        "let",
        "if",
        "then",
        "else",
        "case",
        "data",
        "match",
        "with"
    );

    public static final Arbitrary<String> nameArbitrary = Combinators
        .combine(nameBeginArbitrary, nameContinueArbitrary)
        .as((begin, cont) -> begin + cont)
        .filter(name -> !reservedWords.contains(name));

    private static final Arbitrary<Boolean> booleanArbitrary = Arbitraries.of(true, false);

    public static Arbitrary<BooleanNode<Attributes>> booleanNode = booleanArbitrary.map(bool -> {
        return SyntaxNodes.boolNode(Meta.nameless(Type.BOOLEAN), bool);
    });

    public static Arbitrary<CharNode<Attributes>> charNode = Arbitraries.chars().map(character -> {
        return SyntaxNodes.charNode(Meta.nameless(Type.CHAR), character);
    });

    public static Arbitrary<StringNode<Attributes>> stringNode = Arbitraries.strings().ascii().ofMaxLength(50).map(string -> {
        return SyntaxNodes.stringNode(Meta.nameless(Type.STRING), string);
    });

    public static Arbitrary<IntNode<Attributes>> intNode = Arbitraries.integers().greaterOrEqual(0).map(i -> {
        return SyntaxNodes.intNode(Meta.nameless(Type.INT), i);
    });

    public static Arbitrary<LongNode<Attributes>> longNode = Arbitraries.longs().greaterOrEqual(0L).map(l -> {
        return SyntaxNodes.longNode(Meta.nameless(Type.LONG), l);
    });

    public static Arbitrary<FloatNode<Attributes>> floatNode = Arbitraries.floats().greaterOrEqual(0F).map(f -> {
        return SyntaxNodes.floatNode(Meta.nameless(Type.FLOAT), f);
    });

    public static Arbitrary<DoubleNode<Attributes>> doubleNode = Arbitraries.doubles().greaterOrEqual(0D).map(d -> {
        return SyntaxNodes.doubleNode(Meta.nameless(Type.DOUBLE), d);
    });

    public static Arbitrary<LiteralNode<Attributes>> literalNode = Arbitraries.oneOf(booleanNode, charNode, stringNode, intNode, longNode, floatNode, doubleNode);

    public static Arbitrary<? extends LiteralNode<Attributes>> literalWithType(Type typ) {
        if (Type.BOOLEAN.equals(typ)) {
            return booleanNode;
        } else if (Type.CHAR.equals(typ)) {
            return charNode;
        } else if (Type.STRING.equals(typ)) {
            return stringNode;
        } else if (Type.INT.equals(typ)) {
            return intNode;
        } else if (Type.LONG.equals(typ)) {
            return longNode;
        } else if (Type.FLOAT.equals(typ)) {
            return floatNode;
        } else if (Type.DOUBLE.equals(typ)) {
            return doubleNode;
        }

        return null;
    }

    public static Arbitrary<? extends ExprNode<Attributes>>[] refWithType(GenEnvironment env, Type typ) {
        Set<Meta<Attributes>> values = env.scopes().toList()
            .toReversed().flatCollect(GenScope::values).toSet();
        return values.stream()
            .filter(meta -> meta.meta().name() instanceof Named && meta.meta().sort().equals(typ))
            .map(meta -> {
                Named name = (Named) meta.meta().name();
                return Arbitraries.just(SyntaxNodes.refNode(meta, name.localName()));
            }).toArray(Arbitrary[]::new);
    }

    public static Arbitrary<IfNode<Attributes>> ifNode(GenEnvironment env) {
        return Combinators.combine(
            Arbitraries.oneOf(booleanNode, refWithType(env, Type.BOOLEAN)),
            exprNode(env)
        ).flatAs((cond, cons) -> {
            return exprNodeWithType(env, (Type) cons.meta().meta().sort()).map(alt -> {
                return SyntaxNodes.ifNode(Meta.nameless(getType(cons)), cond, cons, alt);
            });
        });
    }

    public static Arbitrary<IfNode<Attributes>> ifNodeWithType(GenEnvironment env, Type typ) {
        return Combinators.combine(
            Arbitraries.oneOf(booleanNode, refWithType(env, Type.BOOLEAN)),
            exprNodeWithType(env, typ), exprNodeWithType(env, typ)
        ).as((cond, cons, alt) -> {
            return SyntaxNodes.ifNode(Meta.nameless(getType(cons)), cond, cons, alt);
        });
    }

    public static Arbitrary<ReferenceNode<Attributes>> refNode(GenEnvironment env) {
        Set<Meta<Attributes>> values = env.scopes().toList()
            .toReversed().flatCollect(GenScope::values).toSet();
        return Arbitraries.of(values)
            .filter(meta -> meta.meta().name() instanceof Named)
            .map(meta -> {
                Named name = (Named) meta.meta().name();
                return SyntaxNodes.refNode(meta, name.localName());
            });
    }

    public static Arbitrary<? extends ExprNode<Attributes>> exprNode(GenEnvironment env) {
        return Arbitraries.lazy(() -> {
            var generators = Arrays.asList(
                Tuple.of(7, literalNode),
                Tuple.of(1, ifNode(env))
            );
            if (env.scopes().collectInt(scope -> scope.values().size()).sum() > 0) {
                generators.add(Tuple.of(1, refNode(env)));
            }
            return Arbitraries.frequencyOf(generators.toArray(new Tuple.Tuple2[0]));
        });
    }

    public static Arbitrary<? extends ExprNode<Attributes>> exprNodeWithType(GenEnvironment env, Type typ) {
        return Arbitraries.lazy(() -> {
            var generators = Arrays.asList(
                Tuple.of(7, Objects.requireNonNull(literalWithType(typ))),
                Tuple.of(1, ifNodeWithType(env, typ))
            );
            if (env.scopes().collectInt(scope -> scope.values().size()).sum() > 0) {
                generators.addAll(Arrays.stream(refWithType(env, typ)).map(gen -> Tuple.of(1, gen)).toList());
            }
            return Arbitraries.frequencyOf(generators.toArray(new Tuple.Tuple2[0]));
        });
    }
}

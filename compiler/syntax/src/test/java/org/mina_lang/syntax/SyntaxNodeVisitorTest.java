/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.syntax;

import com.opencastsoftware.yvette.Range;
import org.hamcrest.Matcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mina_lang.syntax.TracingSyntaxNodeVisitor.Entry;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mina_lang.syntax.SyntaxNodes.*;

public class SyntaxNodeVisitorTest {

    private static Stream<TracingSyntaxNodeVisitor> visitor() {
        return Stream.of(new TracingSyntaxNodeVisitor());
    }

    private <A extends SyntaxNode> void expectEntries(
            TracingSyntaxNodeVisitor visitor,
            A syntaxNode,
            Matcher<Iterable<? extends Entry>> expected) {
        syntaxNode.accept(visitor);
        assertThat(visitor.getEntries(), expected);
    }

    // Namespaces
    @ParameterizedTest
    @MethodSource("visitor")
    void testVisitNamespace(TracingSyntaxNodeVisitor visitor) {
        /*-
         * namespace Mina/Test/Visitor {
         *     import Mina/Test/Parser.y
         *     let x = y
         * }
         */
        var ns = namespaceNode(
                new Range(0, 0, 4, 0),
                nsIdNode(
                        new Range(0, 7, 0, 24),
                        List.of("Mina", "Test"), "Visitor"),
                List.of(
                        importSymbolsNode(
                                new Range(1, 4, 1, 29),
                                nsIdNode(
                                        new Range(1, 11, 1, 27),
                                        List.of("Mina", "Test"), "Parser"),
                                List.of(importeeNode(new Range(1, 28, 1, 29), "y")))),
                List.of(
                        letNode(
                                new Range(2, 4, 2, 13),
                                "x",
                                refNode(new Range(2, 12, 2, 13), "y"))

                ));

        var expected = contains(
                new Entry(NamespaceIdNode.class, new Range(0, 7, 0, 24)),
                new Entry(NamespaceIdNode.class, new Range(1, 11, 1, 27)),
                new Entry(ImporteeNode.class, new Range(1, 28, 1, 29)),
                new Entry(ImportSymbolsNode.class, new Range(1, 4, 1, 29)),
                new Entry(QualifiedIdNode.class, new Range(2, 12, 2, 13)),
                new Entry(ReferenceNode.class, new Range(2, 12, 2, 13)),
                new Entry(LetNode.class, new Range(2, 4, 2, 13)),
                new Entry(NamespaceNode.class, new Range(0, 0, 4, 0)));

        expectEntries(visitor, ns, expected);
    }

    // Types
    @ParameterizedTest
    @MethodSource("visitor")
    void testVisitQuantifiedType(TracingSyntaxNodeVisitor visitor) {
        /* [A] { A } */
        var quant = quantifiedTypeNode(
                new Range(0, 0, 0, 6),
                List.of(forAllVarNode(new Range(0, 0, 0, 1), "A")),
                typeRefNode(new Range(0, 5, 0, 6), "A"));

        var expected = contains(
                new Entry(ForAllVarNode.class, new Range(0, 0, 0, 1)),
                new Entry(QualifiedIdNode.class, new Range(0, 5, 0, 6)),
                new Entry(TypeReferenceNode.class, new Range(0, 5, 0, 6)),
                new Entry(QuantifiedTypeNode.class, new Range(0, 0, 0, 6)));

        expectEntries(visitor, quant, expected);
    }

    @ParameterizedTest
    @MethodSource("visitor")
    void testVisitExistentiallyQuantifiedType(TracingSyntaxNodeVisitor visitor) {
        /* [?A] { ?A } */
        var quant = quantifiedTypeNode(
                new Range(0, 0, 0, 8),
                List.of(existsVarNode(new Range(0, 0, 0, 2), "A")),
                typeRefNode(new Range(0, 6, 0, 8), "?A"));

        var expected = contains(
                new Entry(ExistsVarNode.class, new Range(0, 0, 0, 2)),
                new Entry(QualifiedIdNode.class, new Range(0, 6, 0, 8)),
                new Entry(TypeReferenceNode.class, new Range(0, 6, 0, 8)),
                new Entry(QuantifiedTypeNode.class, new Range(0, 0, 0, 8)));

        expectEntries(visitor, quant, expected);
    }

    @ParameterizedTest
    @MethodSource("visitor")
    void testVisitFunType(TracingSyntaxNodeVisitor visitor) {
        /* A -> A */
        var funTy = funTypeNode(
                new Range(0, 0, 0, 6),
                List.of(typeRefNode(new Range(0, 0, 0, 1), "A")),
                typeRefNode(new Range(0, 5, 0, 6), "A"));

        var expected = contains(
                new Entry(QualifiedIdNode.class, new Range(0, 0, 0, 1)),
                new Entry(TypeReferenceNode.class, new Range(0, 0, 0, 1)),
                new Entry(QualifiedIdNode.class, new Range(0, 5, 0, 6)),
                new Entry(TypeReferenceNode.class, new Range(0, 5, 0, 6)),
                new Entry(FunTypeNode.class, new Range(0, 0, 0, 6)));

        expectEntries(visitor, funTy, expected);
    }

    @ParameterizedTest
    @MethodSource("visitor")
    void testVisitTypeApply(TracingSyntaxNodeVisitor visitor) {
        /* List[Int] */
        var tyApp = typeApplyNode(
                new Range(0, 0, 0, 9),
                typeRefNode(new Range(0, 0, 0, 4), "List"),
                List.of(typeRefNode(new Range(0, 5, 0, 8), "Int")));

        var expected = contains(
                // List
                new Entry(QualifiedIdNode.class, new Range(0, 0, 0, 4)),
                new Entry(TypeReferenceNode.class, new Range(0, 0, 0, 4)),
                // Int
                new Entry(QualifiedIdNode.class, new Range(0, 5, 0, 8)),
                new Entry(TypeReferenceNode.class, new Range(0, 5, 0, 8)),
                // List[Int]
                new Entry(TypeApplyNode.class, new Range(0, 0, 0, 9)));

        expectEntries(visitor, tyApp, expected);
    }

    // Declarations
    @ParameterizedTest
    @MethodSource("visitor")
    void testVisitLet(TracingSyntaxNodeVisitor visitor) {
        /* let x: Int = y */
        var let = letNode(
                new Range(0, 0, 0, 14),
                "x",
                typeRefNode(new Range(0, 7, 0, 10), "Int"),
                refNode(new Range(0, 13, 0, 14), "y"));

        var expected = contains(
                new Entry(QualifiedIdNode.class, new Range(0, 7, 0, 10)),
                new Entry(TypeReferenceNode.class, new Range(0, 7, 0, 10)),
                new Entry(QualifiedIdNode.class, new Range(0, 13, 0, 14)),
                new Entry(ReferenceNode.class, new Range(0, 13, 0, 14)),
                new Entry(LetNode.class, new Range(0, 0, 0, 14)));

        expectEntries(visitor, let, expected);
    }

    @ParameterizedTest
    @MethodSource("visitor")
    void testVisitLetFn(TracingSyntaxNodeVisitor visitor) {
        /* let id[A](a: A): A = a */
        var letFn = letFnNode(
                new Range(0, 0, 0, 22),
                "id",
                List.of(forAllVarNode(new Range(0, 7, 0, 8), "A")),
                List.of(paramNode(new Range(0, 10, 0, 14), "a",
                        typeRefNode(new Range(0, 13, 0, 14), "A"))),
                typeRefNode(new Range(0, 17, 0, 18), "A"),
                refNode(new Range(0, 21, 0, 22), "a"));

        var expected = contains(
                // A
                new Entry(ForAllVarNode.class, new Range(0, 7, 0, 8)),
                // A
                new Entry(QualifiedIdNode.class, new Range(0, 13, 0, 14)),
                new Entry(TypeReferenceNode.class, new Range(0, 13, 0, 14)),
                // a: A
                new Entry(ParamNode.class, new Range(0, 10, 0, 14)),
                // : A
                new Entry(QualifiedIdNode.class, new Range(0, 17, 0, 18)),
                new Entry(TypeReferenceNode.class, new Range(0, 17, 0, 18)),
                // a
                new Entry(QualifiedIdNode.class, new Range(0, 21, 0, 22)),
                new Entry(ReferenceNode.class, new Range(0, 21, 0, 22)),
                new Entry(LetFnNode.class, new Range(0, 0, 0, 22)));

        expectEntries(visitor, letFn, expected);
    }

    @ParameterizedTest
    @MethodSource("visitor")
    void testVisitData(TracingSyntaxNodeVisitor visitor) {
        /*-
         * data Expr[A] {
         *     case Int(i: Int): Expr[Int]
         *     case Bool(b: Boolean): Expr[Boolean]
         * }
         */
        var data = dataNode(
                new Range(0, 0, 3, 1),
                "Expr",
                List.of(forAllVarNode(new Range(0, 10, 0, 11), "A")),
                List.of(
                        // Int
                        constructorNode(new Range(1, 4, 1, 31), "Int",
                                // i: Int
                                List.of(constructorParamNode(new Range(1, 13, 1, 19), "i",
                                        typeRefNode(new Range(1, 16, 1, 19), "Int"))),
                                // : Expr[Int]
                                Optional.of(typeApplyNode(new Range(1, 22, 1, 31),
                                        typeRefNode(new Range(1, 22, 1, 26), "Expr"),
                                    List.of(typeRefNode(new Range(1, 27, 1, 30), "Int"))))),
                        // Bool
                        constructorNode(new Range(2, 4, 2, 40), "Bool",
                                // b: Boolean
                                List.of(constructorParamNode(new Range(2, 14, 2, 24), "b",
                                        typeRefNode(new Range(2, 17, 2, 24), "Boolean"))),
                                // : Expr[Boolean]
                                Optional.of(typeApplyNode(new Range(2, 27, 2, 40),
                                        typeRefNode(new Range(2, 27, 2, 31), "Expr"),
                                    List.of(typeRefNode(new Range(2, 32, 2, 39),
                                                        "Boolean")))))));

        var expected = contains(
                new Entry(ForAllVarNode.class, new Range(0, 10, 0, 11)),
                // Int
                new Entry(QualifiedIdNode.class, new Range(1, 16, 1, 19)),
                new Entry(TypeReferenceNode.class, new Range(1, 16, 1, 19)),
                // i: Int
                new Entry(ConstructorParamNode.class, new Range(1, 13, 1, 19)),
                // Int
                new Entry(QualifiedIdNode.class, new Range(1, 22, 1, 26)),
                new Entry(TypeReferenceNode.class, new Range(1, 22, 1, 26)),
                // Expr
                new Entry(QualifiedIdNode.class, new Range(1, 27, 1, 30)),
                new Entry(TypeReferenceNode.class, new Range(1, 27, 1, 30)),
                // Expr[Int]
                new Entry(TypeApplyNode.class, new Range(1, 22, 1, 31)),
                new Entry(ConstructorNode.class, new Range(1, 4, 1, 31)),
                // Boolean
                new Entry(QualifiedIdNode.class, new Range(2, 17, 2, 24)),
                new Entry(TypeReferenceNode.class, new Range(2, 17, 2, 24)),
                // b: Boolean
                new Entry(ConstructorParamNode.class, new Range(2, 14, 2, 24)),
                // Expr
                new Entry(QualifiedIdNode.class, new Range(2, 27, 2, 31)),
                new Entry(TypeReferenceNode.class, new Range(2, 27, 2, 31)),
                // Boolean
                new Entry(QualifiedIdNode.class, new Range(2, 32, 2, 39)),
                new Entry(TypeReferenceNode.class, new Range(2, 32, 2, 39)),
                // Expr[Boolean]
                new Entry(TypeApplyNode.class, new Range(2, 27, 2, 40)),
                new Entry(ConstructorNode.class, new Range(2, 4, 2, 40)),
                new Entry(DataNode.class, new Range(0, 0, 3, 1)));

        expectEntries(visitor, data, expected);
    }

    // Expressions
    @ParameterizedTest
    @MethodSource("visitor")
    void testVisitIf(TracingSyntaxNodeVisitor visitor) {
        /* if false then 0 else 1 */
        var ifExpr = ifNode(
                new Range(0, 0, 0, 22),
                boolNode(new Range(0, 3, 0, 8), false),
                intNode(new Range(0, 14, 1), 0),
                intNode(new Range(0, 21, 1), 1));

        var expected = contains(
                new Entry(BooleanNode.class, new Range(0, 3, 0, 8)),
                new Entry(IntNode.class, new Range(0, 14, 1)),
                new Entry(IntNode.class, new Range(0, 21, 1)),
                new Entry(IfNode.class, new Range(0, 0, 0, 22)));

        expectEntries(visitor, ifExpr, expected);
    }

    @ParameterizedTest
    @MethodSource("visitor")
    void testVisitApply(TracingSyntaxNodeVisitor visitor) {
        /* f(x) */
        var apply = applyNode(
                new Range(0, 0, 0, 4),
                refNode(new Range(0, 0, 0, 1), "f"),
                List.of(refNode(new Range(0, 2, 0, 3), "x")));

        var expected = contains(
                new Entry(QualifiedIdNode.class, new Range(0, 0, 0, 1)),
                new Entry(ReferenceNode.class, new Range(0, 0, 0, 1)),
                new Entry(QualifiedIdNode.class, new Range(0, 2, 0, 3)),
                new Entry(ReferenceNode.class, new Range(0, 2, 0, 3)),
                new Entry(ApplyNode.class, new Range(0, 0, 0, 4)));

        expectEntries(visitor, apply, expected);
    }

    @ParameterizedTest
    @MethodSource("visitor")
    void testVisitMatch(TracingSyntaxNodeVisitor visitor) {
        /* match x with { case y -> z } */
        var match = matchNode(
                new Range(0, 0, 0, 28),
                refNode(new Range(0, 6, 0, 7), "x"),
                List.of(
                        caseNode(
                                new Range(0, 15, 0, 26),
                                idPatternNode(new Range(0, 20, 0, 21), "y"),
                                refNode(new Range(0, 25, 0, 26), "z"))));

        var expected = contains(
                new Entry(QualifiedIdNode.class, new Range(0, 6, 1)),
                new Entry(ReferenceNode.class, new Range(0, 6, 1)),
                new Entry(IdPatternNode.class, new Range(0, 20, 1)),
                new Entry(QualifiedIdNode.class, new Range(0, 25, 1)),
                new Entry(ReferenceNode.class, new Range(0, 25, 1)),
                new Entry(CaseNode.class, new Range(0, 15, 0, 26)),
                new Entry(MatchNode.class, new Range(0, 0, 0, 28)));

        expectEntries(visitor, match, expected);
    }

    @ParameterizedTest
    @MethodSource("visitor")
    void testVisitConstructorPattern(TracingSyntaxNodeVisitor visitor) {
        /* case Cons { head, tail: Nil {} } -> head */
        var cse = caseNode(
                new Range(0, 0, 0, 41),
                constructorPatternNode(
                        new Range(0, 5, 0, 33),
                        idNode(new Range(0, 5, 0, 9), "Cons"),
                        List.of(
                                fieldPatternNode(
                                    new Range(0, 12, 0, 16), "head",
                                    idPatternNode(new Range(0, 12, 0, 16), "head")),
                                fieldPatternNode(new Range(0, 18, 0, 30), "tail",
                                        constructorPatternNode(
                                                new Range(0, 24, 0, 30),
                                                idNode(new Range(0, 24, 0, 27), "Nil"),
                                                List.of())))),
                refNode(new Range(0, 37, 0, 41), "head"));

        var expected = contains(
                // Cons
                new Entry(QualifiedIdNode.class, new Range(0, 5, 0, 9)),
                // head
                new Entry(IdPatternNode.class, new Range(0, 12, 0, 16)),
                // head:
                new Entry(FieldPatternNode.class, new Range(0, 12, 0, 16)),
                // Nil
                new Entry(QualifiedIdNode.class, new Range(0, 24, 0, 27)),
                new Entry(ConstructorPatternNode.class, new Range(0, 24, 0, 30)),
                // tail
                new Entry(FieldPatternNode.class, new Range(0, 18, 0, 30)),
                new Entry(ConstructorPatternNode.class, new Range(0, 5, 0, 33)),
                // head
                new Entry(QualifiedIdNode.class, new Range(0, 37, 0, 41)),
                new Entry(ReferenceNode.class, new Range(0, 37, 0, 41)),
                new Entry(CaseNode.class, new Range(0, 0, 0, 41)));

        expectEntries(visitor, cse, expected);
    }

    @ParameterizedTest
    @MethodSource("visitor")
    void testVisitAliasPattern(TracingSyntaxNodeVisitor visitor) {
        /* case a @ b -> a */
        var cse = caseNode(
                new Range(0, 0, 0, 15),
                aliasPatternNode(new Range(0, 5, 0, 9),
                        "a",
                        idPatternNode(new Range(0, 8, 0, 9), "b")),
                refNode(new Range(0, 14, 0, 15), "a"));

        var expected = contains(
                new Entry(IdPatternNode.class, new Range(0, 8, 1)),
                new Entry(AliasPatternNode.class, new Range(0, 5, 4)),
                new Entry(QualifiedIdNode.class, new Range(0, 14, 1)),
                new Entry(ReferenceNode.class, new Range(0, 14, 1)),
                new Entry(CaseNode.class, new Range(0, 0, 0, 15)));

        expectEntries(visitor, cse, expected);
    }

    @ParameterizedTest
    @MethodSource("visitor")
    void testVisitLiteralPattern(TracingSyntaxNodeVisitor visitor) {
        /* case 1.234e+2f -> x */
        var cse = caseNode(
                new Range(0, 0, 0, 19),
                literalPatternNode(new Range(0, 5, 0, 14),
                        floatNode(new Range(0, 5, 0, 14), 1.234e+2f)),
                refNode(new Range(0, 18, 0, 19), "x"));

        var expected = contains(
                new Entry(FloatNode.class, new Range(0, 5, 0, 14)),
                new Entry(LiteralPatternNode.class, new Range(0, 5, 0, 14)),
                new Entry(QualifiedIdNode.class, new Range(0, 18, 1)),
                new Entry(ReferenceNode.class, new Range(0, 18, 1)),
                new Entry(CaseNode.class, new Range(0, 0, 0, 19)));

        expectEntries(visitor, cse, expected);
    }

    @ParameterizedTest
    @MethodSource("visitor")
    void testVisitBlock(TracingSyntaxNodeVisitor visitor) {
        /*-
         * {
         *     let x = "a"
         *     let y = 'a'
         *     2.0
         * }
         */
        var block = blockNode(
                new Range(0, 0, 4, 1),
                List.of(
                        letNode(new Range(1, 4, 1, 15), "x", stringNode(new Range(1, 12, 1, 15), "a")),
                        letNode(new Range(2, 4, 2, 15), "y", charNode(new Range(2, 12, 2, 15), 'a'))),
                doubleNode(new Range(3, 4, 3, 7), 2.0));

        var expected = contains(
                new Entry(StringNode.class, new Range(1, 12, 1, 15)),
                new Entry(LetNode.class, new Range(1, 4, 1, 15)),
                new Entry(CharNode.class, new Range(2, 12, 2, 15)),
                new Entry(LetNode.class, new Range(2, 4, 2, 15)),
                new Entry(DoubleNode.class, new Range(3, 4, 3, 7)),
                new Entry(BlockNode.class, new Range(0, 0, 4, 1)));

        expectEntries(visitor, block, expected);
    }

    @ParameterizedTest
    @MethodSource("visitor")
    void testVisitLambda(TracingSyntaxNodeVisitor visitor) {
        /* () -> 1L */
        var lambda = lambdaNode(
                new Range(0, 0, 0, 8),
                List.of(),
                longNode(new Range(0, 6, 0, 8), 1L));

        var expected = contains(
                new Entry(LongNode.class, new Range(0, 6, 0, 8)),
                new Entry(LambdaNode.class, new Range(0, 0, 0, 8)));

        expectEntries(visitor, lambda, expected);
    }
}

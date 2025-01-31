/*
 * SPDX-FileCopyrightText:  © 2022-2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.parser;

import com.opencastsoftware.yvette.Range;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;
import org.mina_lang.parser.Parser.Visitor;
import org.mina_lang.syntax.BinaryOp;
import org.mina_lang.syntax.NamespaceNode;
import org.mina_lang.syntax.SyntaxNode;
import org.mina_lang.syntax.UnaryOp;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mina_lang.syntax.SyntaxNodes.*;

public class ParserTest {

    void testSuccessfulParse(
            String source,
            NamespaceNode<Void> expected) {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Parser.mina");
        var parsingCollector = new ANTLRDiagnosticReporter(baseCollector, dummyUri);
        var actual = new Parser(parsingCollector).parse(source);
        assertThat("There should be no parsing errors", baseCollector.getErrors(), empty());
        assertThat("The result syntax node should not be null", actual, notNullValue());
        assertThat(actual, equalTo(expected));
    }

    List<String> testFailedParse(String source) {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Parser.mina");
        var parsingCollector = new ANTLRDiagnosticReporter(baseCollector, dummyUri);
        new Parser(parsingCollector).parse(source);
        var errors = baseCollector.getErrors();
        assertThat("There should be parsing errors", errors, not(empty()));
        return errors;
    }

    <A extends ParserRuleContext, B extends SyntaxNode, C extends Visitor<A, B>> void testSuccessfulParse(
            String source,
            Function<Parser, C> visitor,
            Function<MinaParser, A> startRule,
            B expected) {
        testSuccessfulParse(source, new HashSet<>(), visitor, startRule, expected);
    }

    <A extends ParserRuleContext, B extends SyntaxNode, C extends Visitor<A, B>> void testSuccessfulParse(
        String source,
        Set<String> importedNamespaces,
        Function<Parser, C> visitor,
        Function<MinaParser, A> startRule,
        B expected) {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Parser.mina");
        var parsingCollector = new ANTLRDiagnosticReporter(baseCollector, dummyUri);
        var parser = new Parser(parsingCollector);
        var input = CharStreams.fromString(source);
        parser.getImportVisitor().qualifiedNamespaces = importedNamespaces;
        var actual = parser.parse(input, visitor, startRule);
        assertThat("The parser should consume the entire input", input.index(), is(equalTo(input.size())));
        assertThat("There should be no parsing errors", baseCollector.getErrors(), empty());
        assertThat("The result syntax node should not be null", actual, notNullValue());
        assertThat(actual, equalTo(expected));
    }

    <A extends ParserRuleContext, B extends SyntaxNode, C extends Visitor<A, B>> List<String> testFailedParse(
            String source,
            Function<Parser, C> visitor,
            Function<MinaParser, A> startRule) {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Parser.mina");
        var parsingCollector = new ANTLRDiagnosticReporter(baseCollector, dummyUri);
        var parser = new Parser(parsingCollector);
        var input = CharStreams.fromString(source);
        parser.parse(input, visitor, startRule);
        var errors = baseCollector.getErrors();
        var entireInputConsumed = input.index() == input.size();
        if (entireInputConsumed) {
            assertThat("There should be parsing errors", errors, not(empty()));
        }
        return errors;
    }

    // Namespace header
    @Test
    void parseNamespaceHeader() {
        testSuccessfulParse("namespace Mina/Test/Parser {}",
                namespaceNode(
                        new Range(0, 0, 0, 34),
                        nsIdNode(new Range(0, 10, 0, 26), Lists.immutable.of("Mina", "Test"), "Parser"),
                        Lists.immutable.empty(),
                        Lists.immutable.empty()));
    }

    @Test
    void parseNamespaceHeaderEmptyPackage() {
        testSuccessfulParse("namespace Parser {}",
                namespaceNode(
                        new Range(0, 0, 0, 24),
                        nsIdNode(new Range(0, 10, 0, 16), "Parser"),
                        Lists.immutable.empty(),
                        Lists.immutable.empty()));
    }

    @Test
    void parseNamespaceHeaderMissingName() {
        var errors = testFailedParse("namespace {}");
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("mismatched input '{' expecting ID"));
    }

    @Test
    void parseNamespaceHeaderMissingOpenParen() {
        var errors = testFailedParse("namespace Parser }");
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("missing '{' at '}'"));
    }

    @Test
    void parseNamespaceHeaderMissingCloseParen() {
        var errors = testFailedParse("namespace Parser {");
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>'"));
    }

    // Import declarations
    @Test
    void parseImportNamespaceOnly() {
        testSuccessfulParse("import Mina/Test/Parser", Parser::getImportVisitor,
                MinaParser::importDeclaration,
                importQualifiedNode(
                        new Range(0, 0, 0, 23),
                        nsIdNode(new Range(0, 7, 0, 23), Lists.immutable.of("Mina", "Test"), "Parser")));
    }

    @Test
    void parseImportNamespaceWithAlias() {
        testSuccessfulParse("import Mina/Test/Parser as P", Parser::getImportVisitor,
            MinaParser::importDeclaration,
            importQualifiedNode(
                new Range(0, 0, 0, 28),
                nsIdNode(new Range(0, 7, 0, 23), Lists.immutable.of("Mina", "Test"), "Parser"),
                "P"));
    }

    @Test
    void parseImportEmptyPackageNamespaceOnly() {
        testSuccessfulParse("import Parser", Parser::getImportVisitor, MinaParser::importDeclaration,
                importQualifiedNode(
                        new Range(0, 0, 0, 13),
                        nsIdNode(new Range(0, 7, 0, 13), "Parser")));
    }

    @Test
    void parseImportEmptyPackageNamespaceWithAlias() {
        testSuccessfulParse("import Parser as P", Parser::getImportVisitor, MinaParser::importDeclaration,
            importQualifiedNode(
                new Range(0, 0, 0, 18),
                nsIdNode(new Range(0, 7, 0, 13), "Parser"),
                "P"));
    }

    @Test
    void parseImportSymbolWithAlias() {
        testSuccessfulParse("import Mina/Test/Parser.{compilationUnit, importDeclaration as impDecl}",
            Parser::getImportVisitor,
            MinaParser::importDeclaration,
            importSymbolsNode(
                new Range(0, 0, 0, 71),
                nsIdNode(new Range(0, 7, 0, 23), Lists.immutable.of("Mina", "Test"), "Parser"),
                Lists.immutable.of(
                    importeeNode(new Range(0, 25, 0, 40) ,"compilationUnit"),
                    importeeNode(new Range(0, 42, 0, 70), "importDeclaration", Optional.of("impDecl")))));
    }

    @Test
    void parseImportMultipleSymbols() {
        testSuccessfulParse("import Mina/Test/Parser.{compilationUnit, importDeclaration}",
                Parser::getImportVisitor,
                MinaParser::importDeclaration,
                importSymbolsNode(
                        new Range(0, 0, 0, 60),
                        nsIdNode(new Range(0, 7, 0, 23), Lists.immutable.of("Mina", "Test"), "Parser"),
                        Lists.immutable.of(
                            importeeNode(new Range(0, 25, 0, 40) ,"compilationUnit"),
                            importeeNode(new Range(0, 42, 0, 59), "importDeclaration"))));
    }

    @Test
    void parseImportEmptyPackageMultipleSymbols() {
        testSuccessfulParse("import Parser.{compilationUnit, importDeclaration}",
                Parser::getImportVisitor,
                MinaParser::importDeclaration,
                importSymbolsNode(
                        new Range(0, 0, 0, 50),
                        nsIdNode(new Range(0, 7, 0, 13), "Parser"),
                        Lists.immutable.of(
                            importeeNode(new Range(0, 15, 0, 30) ,"compilationUnit"),
                            importeeNode(new Range(0, 32, 0, 49), "importDeclaration"))));
    }

    @Test
    void parseImportSingleSymbol() {
        testSuccessfulParse("import Mina/Test/Parser.ifExpr", Parser::getImportVisitor,
                MinaParser::importDeclaration,
                importSymbolsNode(
                        new Range(0, 0, 0, 30),
                        nsIdNode(new Range(0, 7, 0, 23), Lists.immutable.of("Mina", "Test"), "Parser"),
                        Lists.immutable.of(importeeNode(new Range(0, 24, 0, 30), "ifExpr"))));
    }

    @Test
    void parseImportEmptyPackageSingleSymbol() {
        testSuccessfulParse("import Parser.ifExpr", Parser::getImportVisitor,
                MinaParser::importDeclaration,
                importSymbolsNode(
                        new Range(0, 0, 0, 20),
                        nsIdNode(new Range(0, 7, 0, 13), "Parser"),
                        Lists.immutable.of(importeeNode(new Range(0, 14, 0, 20), "ifExpr"))));
    }

    @Test
    void parseImportNoSelector() {
        var errors = testFailedParse("import", Parser::getImportVisitor, MinaParser::importDeclaration);
        assertThat(errors.get(0), startsWith("no viable alternative at input 'import'"));
    }

    // Types
    @Test
    void parseQuantifiedType() {
        testSuccessfulParse("[A] { A }", Parser::getTypeVisitor, MinaParser::type,
                quantifiedTypeNode(
                        new Range(0, 0, 0, 9),
                        Lists.immutable.of(forAllVarNode(new Range(0, 1, 0, 2), "A")),
                        typeRefNode(new Range(0, 6, 0, 7), "A")));

    }

    @Test
    void parseNestedQuantifiedType() {
        testSuccessfulParse("[A] { [S] { ST[S, A] } }", Parser::getTypeVisitor, MinaParser::type,
                quantifiedTypeNode(
                        new Range(0, 0, 0, 24),
                        Lists.immutable.of(forAllVarNode(new Range(0, 1, 0, 2), "A")),
                        quantifiedTypeNode(
                                new Range(0, 6, 0, 22), Lists.immutable.of(forAllVarNode(new Range(0, 7, 0, 8), "S")),
                                typeApplyNode(
                                    new Range(0, 12, 0, 20),
                                    typeRefNode(new Range(0, 12, 0, 14), "ST"),
                                    Lists.immutable.of(
                                        typeRefNode(new Range(0, 15, 0, 16), "S"),
                                        typeRefNode(new Range(0, 18, 0, 19), "A"))))));
    }

    @Test
    void parseMultiArgQuantifiedType() {
        testSuccessfulParse("[A, B] { A }", Parser::getTypeVisitor, MinaParser::type,
                quantifiedTypeNode(
                        new Range(0, 0, 0, 12),
                        Lists.immutable.of(
                                forAllVarNode(new Range(0, 1, 0, 2), "A"),
                                forAllVarNode(new Range(0, 4, 0, 5), "B")),
                        typeRefNode(new Range(0, 9, 0, 10), "A")));

    }

    @Test
    void parseExistentiallyQuantifiedType() {
        testSuccessfulParse("[?A] { ?A }", Parser::getTypeVisitor, MinaParser::type,
                quantifiedTypeNode(
                        new Range(0, 0, 0, 11),
                        Lists.immutable.of(existsVarNode(new Range(0, 1, 0, 3), "?A")),
                        typeRefNode(new Range(0, 7, 0, 9), "?A")));
    }

    @Test
    void parseQuantifiedTypeMissingBody() {
        var errors = testFailedParse("[A] {}", Parser::getTypeVisitor, MinaParser::type);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("no viable alternative at input '[A] {}'"));
    }

    @Test
    void parseIdFunType() {
        testSuccessfulParse("A -> A", Parser::getTypeVisitor, MinaParser::type,
                funTypeNode(
                        new Range(0, 0, 0, 6),
                        Lists.immutable.of(typeRefNode(new Range(0, 0, 0, 1), "A")),
                        typeRefNode(new Range(0, 5, 0, 6), "A")));

    }

    @Test
    void parseParenthesizedIdFunType() {
        testSuccessfulParse("(A) -> A", Parser::getTypeVisitor, MinaParser::type,
                funTypeNode(
                        new Range(0, 0, 0, 8),
                        Lists.immutable.of(typeRefNode(new Range(0, 1, 0, 2), "A")),
                        typeRefNode(new Range(0, 7, 0, 8), "A")));

    }

    @Test
    void parseNullaryFunType() {
        testSuccessfulParse("() -> A", Parser::getTypeVisitor, MinaParser::type,
                funTypeNode(
                        new Range(0, 0, 0, 7),
                        Lists.immutable.empty(),
                        typeRefNode(new Range(0, 6, 0, 7), "A")));

    }

    @Test
    void parseMultiArgFunType() {
        testSuccessfulParse("(A, B) -> A", Parser::getTypeVisitor, MinaParser::type,
                funTypeNode(
                        new Range(0, 0, 0, 11),
                        Lists.immutable.of(
                                typeRefNode(new Range(0, 1, 0, 2), "A"),
                                typeRefNode(new Range(0, 4, 0, 5), "B")),
                        typeRefNode(new Range(0, 10, 0, 11), "A")));

    }

    @Test
    void parseFunTypeMissingReturnType() {
        var errors = testFailedParse("let foo: A -> = bar", Parser::getDeclarationVisitor,
                MinaParser::declaration);
        assertThat(errors, hasSize(2));
        assertThat(errors.get(0), startsWith("extraneous input '='"));
        assertThat(errors.get(1), startsWith("mismatched input '<EOF>'"));
    }

    @Test
    void parseUnaryTypeApplication() {
        testSuccessfulParse("List[Int]", Parser::getTypeVisitor, MinaParser::type,
                typeApplyNode(
                        new Range(0, 0, 0, 9),
                        typeRefNode(new Range(0, 0, 0, 4), "List"),
                        Lists.immutable
                                .of(typeRefNode(new Range(0, 5, 0, 8), "Int"))));

    }

    @Test
    void parseBinaryTypeApplication() {
        testSuccessfulParse("Either[Error, String]", Parser::getTypeVisitor, MinaParser::type,
                typeApplyNode(
                        new Range(0, 0, 0, 21),
                        typeRefNode(new Range(0, 0, 0, 6), "Either"),
                        Lists.immutable.of(
                                typeRefNode(new Range(0, 7, 0, 12), "Error"),
                                typeRefNode(new Range(0, 14, 0, 20), "String"))));

    }

    @Test
    void parseWildcardTypeApplication() {
        testSuccessfulParse("List[?]", Parser::getTypeVisitor, MinaParser::type,
                typeApplyNode(
                        new Range(0, 0, 0, 7),
                        typeRefNode(new Range(0, 0, 0, 4), "List"),
                        Lists.immutable.of(typeRefNode(new Range(0, 5, 0, 6), "?"))));

    }

    // Declarations
    @Test
    void parseLetDeclaration() {
        testSuccessfulParse("let x = y", Parser::getDeclarationVisitor, MinaParser::declaration,
                letNode(
                        new Range(0, 0, 0, 9),
                        "x",
                        refNode(new Range(0, 8, 0, 9), "y")));
    }

    @Test
    void parseLetDeclarationWithTypeAnnotation() {
        testSuccessfulParse("let x: Int = y", Parser::getDeclarationVisitor, MinaParser::declaration,
                letNode(
                        new Range(0, 0, 0, 14),
                        "x",
                        typeRefNode(new Range(0, 7, 0, 10), "Int"),
                        refNode(new Range(0, 13, 0, 14), "y")));
    }

    @Test
    void parseLetFnDeclarationWithTypeAnnotation() {
        testSuccessfulParse("let id[A](a: A): A = a", Parser::getDeclarationVisitor,
                MinaParser::declaration,
                letFnNode(
                        new Range(0, 0, 0, 22),
                        "id",
                        Lists.immutable.of(forAllVarNode(new Range(0, 7, 0, 8), "A")),
                        Lists.immutable.of(paramNode(new Range(0, 10, 0, 14), "a",
                                typeRefNode(new Range(0, 13, 0, 14), "A"))),
                        typeRefNode(new Range(0, 17, 0, 18), "A"),
                        refNode(new Range(0, 21, 0, 22), "a")));
    }

    @Test
    void parseEmptyDataDeclaration() {
        testSuccessfulParse("data Void[A] {}", Parser::getDeclarationVisitor, MinaParser::declaration,
                dataNode(
                        new Range(0, 0, 0, 15),
                        "Void",
                        Lists.immutable.of(forAllVarNode(new Range(0, 10, 0, 11), "A")),
                        Lists.immutable.empty()));
    }

    @Test
    void parseListDataDeclaration() {
        testSuccessfulParse("data List[A] { case Cons(head: A, tail: List[A]) case Nil() }",
                Parser::getDeclarationVisitor, MinaParser::declaration,
                dataNode(
                        new Range(0, 0, 0, 61),
                        "List",
                        Lists.immutable.of(forAllVarNode(new Range(0, 10, 0, 11), "A")),
                        Lists.immutable.of(
                                // Cons
                                constructorNode(
                                        new Range(0, 15, 0, 48), "Cons",
                                        Lists.immutable.of(
                                                // head: A
                                                constructorParamNode(new Range(0, 25, 0, 32), "head",
                                                        typeRefNode(new Range(0, 31, 0, 32), "A")),
                                                // tail: List[A]
                                                constructorParamNode(new Range(0, 34, 0, 47), "tail",
                                                        typeApplyNode(new Range(0, 40, 0, 47),
                                                                typeRefNode(new Range(0, 40, 0, 44), "List"),
                                                                Lists.immutable.of(typeRefNode(
                                                                        new Range(0, 45, 0, 46), "A"))))),
                                        Optional.empty()),
                                // Nil
                                constructorNode(
                                        new Range(0, 49, 0, 59), "Nil", Lists.immutable.empty(), Optional.empty()))));
    }

    @Test
    void parseGadtDeclaration() {
        testSuccessfulParse("""
                data Expr[A] {
                    case Int(i: Int): Expr[Int]
                    case Bool(b: Boolean): Expr[Boolean]
                }
                """, Parser::getDeclarationVisitor, MinaParser::declaration,
                dataNode(
                        new Range(0, 0, 3, 1),
                        "Expr",
                        Lists.immutable.of(forAllVarNode(new Range(0, 10, 0, 11), "A")),
                        Lists.immutable.of(
                                // Int
                                constructorNode(new Range(1, 4, 1, 31), "Int",
                                        // i: Int
                                        Lists.immutable.of(constructorParamNode(new Range(1, 13, 1, 19), "i",
                                                typeRefNode(new Range(1, 16, 1, 19), "Int"))),
                                        // : Expr[Int]
                                        Optional.of(typeApplyNode(new Range(1, 22, 1, 31),
                                                typeRefNode(new Range(1, 22, 1, 26), "Expr"), Lists.immutable
                                                        .of(typeRefNode(new Range(1, 27, 1, 30), "Int"))))),
                                // Bool
                                constructorNode(new Range(2, 4, 2, 40), "Bool",
                                        // i: Int
                                        Lists.immutable.of(constructorParamNode(new Range(2, 14, 2, 24), "b",
                                                typeRefNode(new Range(2, 17, 2, 24), "Boolean"))),
                                        // : Expr[Int]
                                        Optional.of(typeApplyNode(new Range(2, 27, 2, 40),
                                                typeRefNode(new Range(2, 27, 2, 31), "Expr"), Lists.immutable
                                                        .of(typeRefNode(new Range(2, 32, 2, 39),
                                                                "Boolean"))))))));
    }

    // Block expressions
    @Test
    void parseBlock() {
        testSuccessfulParse("""
                {
                    foo
                }""", Parser::getExprVisitor, MinaParser::expr,
                blockNode(
                        new Range(0, 0, 2, 1),
                        Lists.immutable.empty(),
                        refNode(new Range(1, 4, 1, 7), "foo")));
    }

    @Test
    void parseBlockWithSingleLet() {
        testSuccessfulParse("""
                {
                    let x = 1
                    2
                }""", Parser::getExprVisitor, MinaParser::expr,
                blockNode(
                        new Range(0, 0, 3, 1),
                        Lists.immutable.of(
                                letNode(new Range(1, 4, 1, 13), "x", intNode(new Range(1, 12, 1, 13), 1))),
                        intNode(new Range(2, 4, 2, 5), 2)));
    }

    @Test
    void parseBlockWithMultipleLet() {
        testSuccessfulParse("""
                {
                    let x = 1
                    let y = 'a'
                    2
                }""", Parser::getExprVisitor, MinaParser::expr,
                blockNode(
                        new Range(0, 0, 4, 1),
                        Lists.immutable.of(
                                letNode(new Range(1, 4, 1, 13), "x", intNode(new Range(1, 12, 1, 13), 1)),
                                letNode(new Range(2, 4, 2, 15), "y",
                                        charNode(new Range(2, 12, 2, 15), 'a'))),
                        intNode(new Range(3, 4, 3, 5), 2)));
    }

    @Test
    void parseNestedBlock() {
        testSuccessfulParse("""
                {
                    let x = 1
                    let y = {
                        'a'
                    }
                    2
                }""", Parser::getExprVisitor, MinaParser::expr,
                blockNode(
                        new Range(0, 0, 6, 1),
                        Lists.immutable.of(
                                letNode(new Range(1, 4, 1, 13), "x", intNode(new Range(1, 12, 1, 13), 1)),
                                letNode(new Range(2, 4, 4, 5), "y",
                                        blockNode(new Range(2, 12, 4, 5), charNode(new Range(3, 8, 3, 11), 'a')))),
                        intNode(new Range(5, 4, 5, 5), 2)));
    }

    // Lambda expressions
    @Test
    void parseNullaryLambda() {
        testSuccessfulParse("() -> 1", Parser::getExprVisitor, MinaParser::expr,
                lambdaNode(
                        new Range(0, 0, 0, 7),
                        Lists.immutable.empty(),
                        intNode(new Range(0, 6, 0, 7), 1)));
    }

    @Test
    void parseIdentityLambda() {
        testSuccessfulParse("a -> a", Parser::getExprVisitor, MinaParser::expr,
                lambdaNode(
                        new Range(0, 0, 0, 6),
                        Lists.immutable.of(paramNode(new Range(0, 0, 0, 1), "a")),
                        refNode(new Range(0, 5, 0, 6), "a")));
    }

    @Test
    void parseParenthesizedIdentityLambda() {
        testSuccessfulParse("(a) -> a", Parser::getExprVisitor, MinaParser::expr,
                lambdaNode(
                        new Range(0, 0, 0, 8),
                        Lists.immutable.of(paramNode(new Range(0, 1, 0, 2), "a")),
                        refNode(new Range(0, 7, 0, 8), "a")));
    }

    @Test
    void parseAnnotatedIdentityLambda() {
        testSuccessfulParse("(a: Int) -> a", Parser::getExprVisitor, MinaParser::expr,
                lambdaNode(
                        new Range(0, 0, 0, 13),
                        Lists.immutable.of(
                                paramNode(new Range(0, 1, 0, 7), "a", typeRefNode(new Range(0, 4, 0, 7), "Int"))),
                        refNode(new Range(0, 12, 0, 13), "a")));
    }

    @Test
    void parseMultiArgLambda() {
        testSuccessfulParse("(a, b) -> a", Parser::getExprVisitor, MinaParser::expr,
                lambdaNode(
                        new Range(0, 0, 0, 11),
                        Lists.immutable.of(
                                paramNode(new Range(0, 1, 0, 2), "a"),
                                paramNode(new Range(0, 4, 0, 5), "b")),
                        refNode(new Range(0, 10, 0, 11), "a")));
    }

    @Test
    void parseAnnotatedMultiArgLambda() {
        testSuccessfulParse("(a: Int, b: Boolean) -> a", Parser::getExprVisitor, MinaParser::expr,
                lambdaNode(
                        new Range(0, 0, 0, 25),
                        Lists.immutable.of(
                                paramNode(new Range(0, 1, 0, 7), "a", typeRefNode(new Range(0, 4, 0, 7), "Int")),
                                paramNode(new Range(0, 9, 0, 19), "b",
                                        typeRefNode(new Range(0, 12, 0, 19), "Boolean"))),
                        refNode(new Range(0, 24, 0, 25), "a")));
    }

    @Test
    void parseLambdaMissingBody() {
        var errors = testFailedParse("a ->", Parser::getExprVisitor, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>'"));
    }

    // If expressions
    @Test
    void parseIfExpression() {
        testSuccessfulParse(
                "if false then 0 else 1", Parser::getExprVisitor, MinaParser::expr,
                ifNode(
                        new Range(0, 0, 0, 22),
                        boolNode(new Range(0, 3, 0, 8), false),
                        intNode(new Range(0, 14, 1), 0),
                        intNode(new Range(0, 21, 1), 1)));
    }

    @Test
    void parseIfExpressionMissingCondition() {
        var errors = testFailedParse("if then 0 else 1", Parser::getExprVisitor, MinaParser::expr);
        assertThat(errors.get(0), startsWith("extraneous input 'then'"));
    }

    @Test
    void parseIfExpressionMissingConsequent() {
        var errors = testFailedParse("if false then else 1", Parser::getExprVisitor, MinaParser::expr);
        assertThat(errors.get(0), startsWith("extraneous input 'else'"));
    }

    @Test
    void parseIfExpressionMissingAlternative() {
        var errors = testFailedParse("if false then 0", Parser::getExprVisitor, MinaParser::expr);
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>' expecting {"));
    }

    // Function application
    @Test
    void parseFunctionApplication() {
        testSuccessfulParse("f(x)", Parser::getExprVisitor, MinaParser::expr,
                applyNode(
                        new Range(0, 0, 0, 4),
                        refNode(new Range(0, 0, 0, 1), "f"),
                        Lists.immutable.of(refNode(new Range(0, 2, 0, 3), "x"))));
    }

    @Test
    void parseNullaryFunctionApplication() {
        testSuccessfulParse("f()", Parser::getExprVisitor, MinaParser::expr,
                applyNode(
                        new Range(0, 0, 0, 3),
                        refNode(new Range(0, 0, 0, 1), "f"),
                        Lists.immutable.empty()));
    }

    @Test
    void parseMultiArgFunctionApplication() {
        testSuccessfulParse("f(x, y)", Parser::getExprVisitor, MinaParser::expr,
                applyNode(
                        new Range(0, 0, 0, 7),
                        refNode(new Range(0, 0, 0, 1), "f"),
                        Lists.immutable.of(
                                refNode(new Range(0, 2, 0, 3), "x"),
                                refNode(new Range(0, 5, 0, 6), "y"))));
    }

    @Test
    void parseNestedFunctionApplication() {
        testSuccessfulParse("f(g(x))", Parser::getExprVisitor, MinaParser::expr,
                applyNode(
                        new Range(0, 0, 0, 7),
                        refNode(new Range(0, 0, 0, 1), "f"),
                        Lists.immutable.of(
                                applyNode(
                                        new Range(0, 2, 0, 6),
                                        refNode(new Range(0, 2, 0, 3), "g"),
                                        Lists.immutable.of(refNode(new Range(0, 4, 0, 5), "x"))))));
    }

    @Test
    void parseCurriedFunctionApplication() {
        testSuccessfulParse("f(x)(y)", Parser::getExprVisitor, MinaParser::expr,
                applyNode(
                        new Range(0, 0, 0, 7),
                        applyNode(
                                new Range(0, 0, 0, 4),
                                refNode(new Range(0, 0, 0, 1), "f"),
                                Lists.immutable.of(refNode(new Range(0, 2, 0, 3), "x"))),
                        Lists.immutable.of(refNode(new Range(0, 5, 0, 6), "y"))));
    }

    @Test
    void parseFunctionApplicationWithGroupingParens() {
        testSuccessfulParse("f((x)(y))", Parser::getExprVisitor, MinaParser::expr,
                applyNode(
                        new Range(0, 0, 0, 9),
                        refNode(new Range(0, 0, 0, 1), "f"),
                        Lists.immutable.of(
                                applyNode(
                                        new Range(0, 2, 0, 8),
                                        refNode(new Range(0, 3, 0, 4), "x"),
                                        Lists.immutable.of(refNode(new Range(0, 6, 0, 7), "y"))))));
    }

    @Test
    void parseRedex() {
        testSuccessfulParse("(x -> x)(1)", Parser::getExprVisitor, MinaParser::expr,
                applyNode(
                        new Range(0, 0, 0, 11),
                        lambdaNode(
                                new Range(0, 1, 0, 7),
                                Lists.immutable.of(paramNode(new Range(0, 1, 0, 2), "x")),
                                refNode(new Range(0, 6, 0, 7), "x")),
                        Lists.immutable.of(intNode(new Range(0, 9, 0, 10), 1))));
    }

    // Member selection
    @Test
    void parseSelect() {
        testSuccessfulParse("a.x", Parser::getExprVisitor, MinaParser::expr,
            selectNode(
                new Range(0, 0, 0, 3),
                refNode(new Range(0, 0, 0, 1), "a"),
                refNode(new Range(0, 2, 0, 3), "x")));
    }

    @Test
    void parseIntLiteralSelect() {
        testSuccessfulParse("1.x", Parser::getExprVisitor, MinaParser::expr,
            selectNode(
                new Range(0, 0, 0, 3),
                intNode(new Range(0, 0, 0, 1), 1),
                refNode(new Range(0, 2, 0, 3), "x")));
    }

    @Test
    void parseStringLiteralSelect() {
        testSuccessfulParse("\"a\".x", Parser::getExprVisitor, MinaParser::expr,
            selectNode(
                new Range(0, 0, 0, 5),
                stringNode(new Range(0, 0, 0, 3), "a"),
                refNode(new Range(0, 4, 0, 5), "x")));
    }

    @Test
    void parseParenSelect() {
        testSuccessfulParse("(a).x", Parser::getExprVisitor, MinaParser::expr,
            selectNode(
                new Range(0, 0, 0, 5),
                refNode(new Range(0, 1, 0, 2), "a"),
                refNode(new Range(0, 4, 0, 5), "x")));
    }

    @Test
    void parseSelectChain() {
        testSuccessfulParse("a.x.y", Parser::getExprVisitor, MinaParser::expr,
            selectNode(
                new Range(0, 0, 0, 5),
                selectNode(
                    new Range(0, 0, 0, 3),
                    refNode(new Range(0, 0, 0, 1), "a"),
                    refNode(new Range(0, 2, 0, 3), "x")),
                refNode(new Range(0, 4, 0, 5), "y")));
    }

    @Test
    void parseApplyFromSelect() {
        testSuccessfulParse("f.x(y)", Parser::getExprVisitor, MinaParser::expr,
            applyNode(
                new Range(0, 0, 0, 6),
                selectNode(
                    new Range(0, 0, 0, 3),
                    refNode(new Range(0, 0, 0, 1), "f"),
                    refNode(new Range(0, 2, 0, 3), "x")),
                Lists.immutable.of(refNode(new Range(0, 4, 0, 5), "y"))));
    }

    @Test
    void parseSelectFromApply() {
        testSuccessfulParse("f(x).y", Parser::getExprVisitor, MinaParser::expr,
            selectNode(
                new Range(0, 0, 0, 6),
                applyNode(
                    new Range(0, 0, 0, 4),
                    refNode(new Range(0, 0, 0, 1), "f"),
                    Lists.immutable.of(refNode(new Range(0, 2, 0, 3), "x"))),
                refNode(new Range(0, 5, 0, 6), "y")));
    }

    @Test
    void parseSelectApplyChain() {
        testSuccessfulParse("a.f(x).g(y)", Parser::getExprVisitor, MinaParser::expr,
            applyNode(
                new Range(0, 0, 0, 11),
                selectNode(
                    new Range(0, 0, 0, 8),
                    applyNode(
                        new Range(0, 0, 0, 6),
                        selectNode(
                            new Range(0, 0, 0, 3),
                            refNode(new Range(0, 0, 0, 1), "a"),
                            refNode(new Range(0, 2, 0, 3), "f")),
                        Lists.immutable.of(refNode(new Range(0, 4, 0, 5), "x"))),
                    refNode(new Range(0, 7, 0, 8), "g")),
                Lists.immutable.of(refNode(new Range(0, 9, 0, 10), "y"))));
    }

    // Unary operators
    @Test
    void parseNegationOperator() {
        testSuccessfulParse("-x", Parser::getExprVisitor, MinaParser::expr,
            unaryOpNode(
                new Range(0, 0, 0, 2),
                UnaryOp.NEGATE,
                refNode(new Range(0, 1, 0, 2), "x")));
    }

    @Test
    void parseNotOperator() {
        testSuccessfulParse("!x", Parser::getExprVisitor, MinaParser::expr,
            unaryOpNode(
                new Range(0, 0, 0, 2),
                UnaryOp.BOOLEAN_NOT,
                refNode(new Range(0, 1, 0, 2), "x")));
    }

    @Test
    void parseBitwiseNotOperator() {
        testSuccessfulParse("~x", Parser::getExprVisitor, MinaParser::expr,
            unaryOpNode(
                new Range(0, 0, 0, 2),
                UnaryOp.BITWISE_NOT,
                refNode(new Range(0, 1, 0, 2), "x")));
    }

    // Binary operators
    @Test
    void parseMultiplyOperator() {
        testSuccessfulParse("x * y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 5),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.MULTIPLY,
                refNode(new Range(0, 4, 0, 5), "y")));
    }

    @Test
    void multiplyOperatorLeftAssociative() {
        testSuccessfulParse("x * y * z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.MULTIPLY,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.MULTIPLY,
                refNode(new Range(0, 8, 0, 9), "z")));
    }

    @Test
    void applicationHasPrecedenceOverMultiply() {
        testSuccessfulParse("x * y(z)", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 8),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.MULTIPLY,
                applyNode(
                    new Range(0, 4, 0, 8),
                    refNode(new Range(0, 4, 0, 5), "y"),
                    Lists.immutable.of(refNode(new Range(0, 6, 0, 7), "z")))));
    }

    @Test
    void unaryNegateHasPrecedenceOverMultiply() {
        testSuccessfulParse("-x * y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 6),
                unaryOpNode(
                    new Range(0, 0, 0, 2),
                    UnaryOp.NEGATE,
                    refNode(new Range(0, 1, 0, 2), "x")),
                BinaryOp.MULTIPLY,
                refNode(new Range(0, 5, 0, 6), "y")));
    }

    @Test
    void parseDivideOperator() {
        testSuccessfulParse("x / y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 5),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.DIVIDE,
                refNode(new Range(0, 4, 0, 5), "y")));
    }

    @Test
    void divideOperatorLeftAssociative() {
        testSuccessfulParse("x / y / z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.DIVIDE,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.DIVIDE,
                refNode(new Range(0, 8, 0, 9), "z")));
    }

    @Test
    void divideHasEqualPrecedenceToMultiply() {
        testSuccessfulParse("x / y * z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.DIVIDE,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.MULTIPLY,
                refNode(new Range(0, 8, 0, 9), "z")));

        testSuccessfulParse("x * y / z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.MULTIPLY,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.DIVIDE,
                refNode(new Range(0, 8, 0, 9), "z")));
    }

    @Test
    void parseModulusOperator() {
        testSuccessfulParse("x % y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 5),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.MODULUS,
                refNode(new Range(0, 4, 0, 5), "y")));
    }

    @Test
    void modulusOperatorLeftAssociative() {
        testSuccessfulParse("x % y % z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.MODULUS,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.MODULUS,
                refNode(new Range(0, 8, 0, 9), "z")));
    }

    @Test
    void modulusHasEqualPrecedenceToMultiply() {
        testSuccessfulParse("x % y * z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.MODULUS,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.MULTIPLY,
                refNode(new Range(0, 8, 0, 9), "z")));

        testSuccessfulParse("x * y % z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.MULTIPLY,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.MODULUS,
                refNode(new Range(0, 8, 0, 9), "z")));
    }

    @Test
    void parseAdditionOperator() {
        testSuccessfulParse("x + y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 5),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.ADD,
                refNode(new Range(0, 4, 0, 5), "y")));
    }

    @Test
    void additionOperatorLeftAssociative() {
        testSuccessfulParse("x + y + z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.ADD,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.ADD,
                refNode(new Range(0, 8, 0, 9), "z")));
    }

    @Test
    void multiplyHasPrecedenceOverAddition() {
        testSuccessfulParse("x + y * z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.ADD,
                binaryOpNode(
                    new Range(0, 4, 0, 9),
                    refNode(new Range(0, 4, 0, 5), "y"),
                    BinaryOp.MULTIPLY,
                    refNode(new Range(0, 8, 0, 9), "z"))));
    }

    @Test
    void parseSubtractionOperator() {
        testSuccessfulParse("x - y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 5),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.SUBTRACT,
                refNode(new Range(0, 4, 0, 5), "y")));
    }

    @Test
    void subtractionOperatorLeftAssociative() {
        testSuccessfulParse("x - y - z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.SUBTRACT,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.SUBTRACT,
                refNode(new Range(0, 8, 0, 9), "z")));
    }

    @Test
    void subtractionHasEqualPrecedenceToAddition() {
        testSuccessfulParse("x + y - z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.ADD,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.SUBTRACT,
                refNode(new Range(0, 8, 0, 9), "z")));

        testSuccessfulParse("x - y + z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.SUBTRACT,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.ADD,
                refNode(new Range(0, 8, 0, 9), "z")));
    }

    @Test
    void parseLeftShiftOperator() {
        testSuccessfulParse("x << y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 6),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.SHIFT_LEFT,
                refNode(new Range(0, 5, 0, 6), "y")));
    }

    @Test
    void leftShiftOperatorLeftAssociative() {
        testSuccessfulParse("x << y << z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 11),
                binaryOpNode(
                    new Range(0, 0, 0, 6),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.SHIFT_LEFT,
                    refNode(new Range(0, 5, 0, 6), "y")),
                BinaryOp.SHIFT_LEFT,
                refNode(new Range(0, 10, 0, 11), "z")));
    }

    @Test
    void additionHasPrecedenceOverLeftShift() {
        testSuccessfulParse("x << y + z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 10),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.SHIFT_LEFT,
                binaryOpNode(
                    new Range(0, 5, 0, 10),
                    refNode(new Range(0, 5, 0, 6), "y"),
                    BinaryOp.ADD,
                    refNode(new Range(0, 9, 0, 10), "z"))));
    }

    @Test
    void parseRightShiftOperator() {
        testSuccessfulParse("x >> y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 6),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.SHIFT_RIGHT,
                refNode(new Range(0, 5, 0, 6), "y")));
    }

    @Test
    void rightShiftOperatorLeftAssociative() {
        testSuccessfulParse("x >> y >> z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 11),
                binaryOpNode(
                    new Range(0, 0, 0, 6),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.SHIFT_RIGHT,
                    refNode(new Range(0, 5, 0, 6), "y")),
                BinaryOp.SHIFT_RIGHT,
                refNode(new Range(0, 10, 0, 11), "z")));
    }

    @Test
    void rightShiftHasEqualPrecedenceToLeftShift() {
        testSuccessfulParse("x << y >> z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 11),
                binaryOpNode(
                    new Range(0, 0, 0, 6),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.SHIFT_LEFT,
                    refNode(new Range(0, 5, 0, 6), "y")),
                BinaryOp.SHIFT_RIGHT,
                refNode(new Range(0, 10, 0, 11), "z")));

        testSuccessfulParse("x >> y << z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 11),
                binaryOpNode(
                    new Range(0, 0, 0, 6),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.SHIFT_RIGHT,
                    refNode(new Range(0, 5, 0, 6), "y")),
                BinaryOp.SHIFT_LEFT,
                refNode(new Range(0, 10, 0, 11), "z")));
    }

    @Test
    void parseUnsignedRightShiftOperator() {
        testSuccessfulParse("x >>> y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 7),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.UNSIGNED_SHIFT_RIGHT,
                refNode(new Range(0, 6, 0, 7), "y")));
    }

    @Test
    void unsignedRightShiftOperatorLeftAssociative() {
        testSuccessfulParse("x >>> y >>> z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 13),
                binaryOpNode(
                    new Range(0, 0, 0, 7),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.UNSIGNED_SHIFT_RIGHT,
                    refNode(new Range(0, 6, 0, 7), "y")),
                BinaryOp.UNSIGNED_SHIFT_RIGHT,
                refNode(new Range(0, 12, 0, 13), "z")));
    }

    @Test
    void unsignedRightShiftHasEqualPrecedenceToLeftShift() {
        testSuccessfulParse("x << y >>> z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 12),
                binaryOpNode(
                    new Range(0, 0, 0, 6),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.SHIFT_LEFT,
                    refNode(new Range(0, 5, 0, 6), "y")),
                BinaryOp.UNSIGNED_SHIFT_RIGHT,
                refNode(new Range(0, 11, 0, 12), "z")));

        testSuccessfulParse("x >>> y << z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 12),
                binaryOpNode(
                    new Range(0, 0, 0, 7),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.UNSIGNED_SHIFT_RIGHT,
                    refNode(new Range(0, 6, 0, 7), "y")),
                BinaryOp.SHIFT_LEFT,
                refNode(new Range(0, 11, 0, 12), "z")));
    }

    @Test
    void parseBitwiseAndOperator() {
        testSuccessfulParse("x & y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 5),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.BITWISE_AND,
                refNode(new Range(0, 4, 0, 5), "y")));
    }

    @Test
    void bitwiseAndOperatorLeftAssociative() {
        testSuccessfulParse("x & y & z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.BITWISE_AND,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.BITWISE_AND,
                refNode(new Range(0, 8, 0, 9), "z")));
    }

    @Test
    void leftShiftHasPrecedenceOverBitwiseAnd() {
        testSuccessfulParse("x & y << z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 10),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.BITWISE_AND,
                binaryOpNode(
                    new Range(0, 4, 0, 10),
                    refNode(new Range(0, 4, 0, 5), "y"),
                    BinaryOp.SHIFT_LEFT,
                    refNode(new Range(0, 9, 0, 10), "z"))));
    }

    @Test
    void parseBitwiseOrOperator() {
        testSuccessfulParse("x | y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 5),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.BITWISE_OR,
                refNode(new Range(0, 4, 0, 5), "y")));
    }

    @Test
    void bitwiseOrOperatorLeftAssociative() {
        testSuccessfulParse("x | y | z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.BITWISE_OR,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.BITWISE_OR,
                refNode(new Range(0, 8, 0, 9), "z")));
    }

    @Test
    void bitwiseAndHasPrecedenceOverBitwiseOr() {
        testSuccessfulParse("x | y & z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.BITWISE_OR,
                binaryOpNode(
                    new Range(0, 4, 0, 9),
                    refNode(new Range(0, 4, 0, 5), "y"),
                    BinaryOp.BITWISE_AND,
                    refNode(new Range(0, 8, 0, 9), "z"))));
    }

    @Test
    void parseBitwiseXorOperator() {
        testSuccessfulParse("x ^ y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 5),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.BITWISE_XOR,
                refNode(new Range(0, 4, 0, 5), "y")));
    }

    @Test
    void bitwiseXorOperatorLeftAssociative() {
        testSuccessfulParse("x ^ y ^ z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.BITWISE_XOR,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.BITWISE_XOR,
                refNode(new Range(0, 8, 0, 9), "z")));
    }

    @Test
    void bitwiseXorHasEqualPrecedenceToBitwiseOr() {
        testSuccessfulParse("x ^ y | z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.BITWISE_XOR,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.BITWISE_OR,
                refNode(new Range(0, 8, 0, 9), "z")));

        testSuccessfulParse("x | y ^ z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.BITWISE_OR,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.BITWISE_XOR,
                refNode(new Range(0, 8, 0, 9), "z")));
    }

    @Test
    void parseLessThanOperator() {
        testSuccessfulParse("x < y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 5),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.LESS_THAN,
                refNode(new Range(0, 4, 0, 5), "y")));
    }

    @Test
    void lessThanOperatorLeftAssociative() {
        testSuccessfulParse("x < y < z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.LESS_THAN,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.LESS_THAN,
                refNode(new Range(0, 8, 0, 9), "z")));
    }

    @Test
    void bitwiseOrHasPrecedenceOverLessThan() {
        testSuccessfulParse("x < y | z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.LESS_THAN,
                binaryOpNode(
                    new Range(0, 4, 0, 9),
                    refNode(new Range(0, 4, 0, 5), "y"),
                    BinaryOp.BITWISE_OR,
                    refNode(new Range(0, 8, 0, 9), "z"))));
    }

    @Test
    void parseLessThanOrEqualOperator() {
        testSuccessfulParse("x <= y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 6),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.LESS_THAN_EQUAL,
                refNode(new Range(0, 5, 0, 6), "y")));
    }

    @Test
    void lessThanOrEqualOperatorLeftAssociative() {
        testSuccessfulParse("x <= y <= z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 11),
                binaryOpNode(
                    new Range(0, 0, 0, 6),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.LESS_THAN_EQUAL,
                    refNode(new Range(0, 5, 0, 6), "y")),
                BinaryOp.LESS_THAN_EQUAL,
                refNode(new Range(0, 10, 0, 11), "z")));
    }

    @Test
    void lessThanOrEqualHasEqualPrecedenceToLessThan() {
        testSuccessfulParse("x <= y < z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 10),
                binaryOpNode(
                    new Range(0, 0, 0, 6),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.LESS_THAN_EQUAL,
                    refNode(new Range(0, 5, 0, 6), "y")),
                BinaryOp.LESS_THAN,
                refNode(new Range(0, 9, 0, 10), "z")));

        testSuccessfulParse("x < y <= z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 10),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.LESS_THAN,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.LESS_THAN_EQUAL,
                refNode(new Range(0, 9, 0, 10), "z")));
    }

    @Test
    void parseGreaterThanOperator() {
        testSuccessfulParse("x > y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 5),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.GREATER_THAN,
                refNode(new Range(0, 4, 0, 5), "y")));
    }

    @Test
    void greaterThanOperatorLeftAssociative() {
        testSuccessfulParse("x > y > z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.GREATER_THAN,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.GREATER_THAN,
                refNode(new Range(0, 8, 0, 9), "z")));
    }

    @Test
    void greaterThanHasEqualPrecedenceToLessThan() {
        testSuccessfulParse("x > y < z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.GREATER_THAN,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.LESS_THAN,
                refNode(new Range(0, 8, 0, 9), "z")));

        testSuccessfulParse("x < y > z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 9),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.LESS_THAN,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.GREATER_THAN,
                refNode(new Range(0, 8, 0, 9), "z")));
    }

    @Test
    void parseGreaterThanOrEqualOperator() {
        testSuccessfulParse("x >= y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 6),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.GREATER_THAN_EQUAL,
                refNode(new Range(0, 5, 0, 6), "y")));
    }

    @Test
    void greaterThanOrEqualOperatorLeftAssociative() {
        testSuccessfulParse("x >= y >= z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 11),
                binaryOpNode(
                    new Range(0, 0, 0, 6),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.GREATER_THAN_EQUAL,
                    refNode(new Range(0, 5, 0, 6), "y")),
                BinaryOp.GREATER_THAN_EQUAL,
                refNode(new Range(0, 10, 0, 11), "z")));
    }

    @Test
    void greaterOrEqualThanHasEqualPrecedenceToLessThan() {
        testSuccessfulParse("x >= y < z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 10),
                binaryOpNode(
                    new Range(0, 0, 0, 6),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.GREATER_THAN_EQUAL,
                    refNode(new Range(0, 5, 0, 6), "y")),
                BinaryOp.LESS_THAN,
                refNode(new Range(0, 9, 0, 10), "z")));

        testSuccessfulParse("x < y >= z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 10),
                binaryOpNode(
                    new Range(0, 0, 0, 5),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.LESS_THAN,
                    refNode(new Range(0, 4, 0, 5), "y")),
                BinaryOp.GREATER_THAN_EQUAL,
                refNode(new Range(0, 9, 0, 10), "z")));
    }

    @Test
    void parseEqualOperator() {
        testSuccessfulParse("x == y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 6),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.EQUAL,
                refNode(new Range(0, 5, 0, 6), "y")));
    }

    @Test
    void equalOperatorLeftAssociative() {
        testSuccessfulParse("x == y == z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 11),
                binaryOpNode(
                    new Range(0, 0, 0, 6),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.EQUAL,
                    refNode(new Range(0, 5, 0, 6), "y")),
                BinaryOp.EQUAL,
                refNode(new Range(0, 10, 0, 11), "z")));
    }

    @Test
    void lessThanHasPrecedenceOverEquals() {
        testSuccessfulParse("x == y < z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 10),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.EQUAL,
                binaryOpNode(
                    new Range(0, 5, 0, 10),
                    refNode(new Range(0, 5, 0, 6), "y"),
                    BinaryOp.LESS_THAN,
                    refNode(new Range(0, 9, 0, 10), "z"))));
    }

    @Test
    void parseNotEqualOperator() {
        testSuccessfulParse("x != y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 6),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.NOT_EQUAL,
                refNode(new Range(0, 5, 0, 6), "y")));
    }

    @Test
    void notEqualOperatorLeftAssociative() {
        testSuccessfulParse("x != y != z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 11),
                binaryOpNode(
                    new Range(0, 0, 0, 6),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.NOT_EQUAL,
                    refNode(new Range(0, 5, 0, 6), "y")),
                BinaryOp.NOT_EQUAL,
                refNode(new Range(0, 10, 0, 11), "z")));
    }

    @Test
    void notEqualHasEqualPrecedenceToEqual() {
        testSuccessfulParse("x == y != z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 11),
                binaryOpNode(
                    new Range(0, 0, 0, 6),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.EQUAL,
                    refNode(new Range(0, 5, 0, 6), "y")),
                BinaryOp.NOT_EQUAL,
                refNode(new Range(0, 10, 0, 11), "z")));

        testSuccessfulParse("x != y == z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 11),
                binaryOpNode(
                    new Range(0, 0, 0, 6),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.NOT_EQUAL,
                    refNode(new Range(0, 5, 0, 6), "y")),
                BinaryOp.EQUAL,
                refNode(new Range(0, 10, 0, 11), "z")));
    }

    @Test
    void parseBooleanAndOperator() {
        testSuccessfulParse("x && y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 6),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.BOOLEAN_AND,
                refNode(new Range(0, 5, 0, 6), "y")));
    }

    @Test
    void andOperatorLeftAssociative() {
        testSuccessfulParse("x && y && z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 11),
                binaryOpNode(
                    new Range(0, 0, 0, 6),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.BOOLEAN_AND,
                    refNode(new Range(0, 5, 0, 6), "y")),
                BinaryOp.BOOLEAN_AND,
                refNode(new Range(0, 10, 0, 11), "z")));
    }

    @Test
    void equalsHasPrecedenceOverAnd() {
        testSuccessfulParse("x && y == z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 11),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.BOOLEAN_AND,
                binaryOpNode(
                    new Range(0, 5, 0, 11),
                    refNode(new Range(0, 5, 0, 6), "y"),
                    BinaryOp.EQUAL,
                    refNode(new Range(0, 10, 0, 11), "z"))));
    }

    @Test
    void parseBooleanOrOperator() {
        testSuccessfulParse("x || y", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 6),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.BOOLEAN_OR,
                refNode(new Range(0, 5, 0, 6), "y")));
    }

    @Test
    void orOperatorLeftAssociative() {
        testSuccessfulParse("x || y || z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 11),
                binaryOpNode(
                    new Range(0, 0, 0, 6),
                    refNode(new Range(0, 0, 0, 1), "x"),
                    BinaryOp.BOOLEAN_OR,
                    refNode(new Range(0, 5, 0, 6), "y")),
                BinaryOp.BOOLEAN_OR,
                refNode(new Range(0, 10, 0, 11), "z")));
    }

    @Test
    void andHasPrecedenceOverOr() {
        testSuccessfulParse("x || y && z", Parser::getExprVisitor, MinaParser::expr,
            binaryOpNode(
                new Range(0, 0, 0, 11),
                refNode(new Range(0, 0, 0, 1), "x"),
                BinaryOp.BOOLEAN_OR,
                binaryOpNode(
                    new Range(0, 5, 0, 11),
                    refNode(new Range(0, 5, 0, 6), "y"),
                    BinaryOp.BOOLEAN_AND,
                    refNode(new Range(0, 10, 0, 11), "z"))));
    }

    // Match expressions
    @Test
    void parseNoAltMatchExpression() {
        testSuccessfulParse("match x with {}", Parser::getExprVisitor, MinaParser::expr,
                matchNode(
                        new Range(0, 0, 0, 15),
                        refNode(new Range(0, 6, 0, 7), "x"),
                        Lists.immutable.empty()));
    }

    @Test
    void parseIdPatternMatchExpression() {
        testSuccessfulParse("match x with { case y -> z }", Parser::getExprVisitor, MinaParser::expr,
                matchNode(
                        new Range(0, 0, 0, 28),
                        refNode(new Range(0, 6, 0, 7), "x"),
                        Lists.immutable.of(
                                caseNode(
                                        new Range(0, 15, 0, 26),
                                        idPatternNode(new Range(0, 20, 0, 21), "y"),
                                        refNode(new Range(0, 25, 0, 26), "z")))));
    }

    @Test
    void parseIdPatternAlias() {
        testSuccessfulParse("case x @ y -> x", Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 15),
                        aliasPatternNode(new Range(0, 5, 0, 10), "x", idPatternNode(new Range(0, 9, 0, 10), "y")),
                        refNode(new Range(0, 14, 0, 15), "x")));
    }

    @Test
    void parseLiteralIntPattern() {
        testSuccessfulParse("case 1 -> x", Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 11),
                        literalPatternNode(new Range(0, 5, 0, 6), intNode(new Range(0, 5, 0, 6), 1)),
                        refNode(new Range(0, 10, 0, 11), "x")));
    }

    @Test
    void parseLiteralIntPatternAlias() {
        testSuccessfulParse("case x @ 1 -> x", Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 15),
                        aliasPatternNode(new Range(0, 5, 0, 10), "x",
                                literalPatternNode(new Range(0, 9, 0, 10), intNode(new Range(0, 9, 0, 10), 1))),
                        refNode(new Range(0, 14, 0, 15), "x")));
    }

    @Test
    void parseLiteralLongPattern() {
        testSuccessfulParse("case 9_999_999L -> x", Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 20),
                        literalPatternNode(new Range(0, 5, 0, 15),
                                longNode(new Range(0, 5, 0, 15), 9999999L)),
                        refNode(new Range(0, 19, 0, 20), "x")));
    }

    @Test
    void parseLiteralBooleanPattern() {
        testSuccessfulParse("case true -> x", Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 14),
                        literalPatternNode(new Range(0, 5, 0, 9),
                                boolNode(new Range(0, 5, 0, 9), true)),
                        refNode(new Range(0, 13, 0, 14), "x")));
    }

    @Test
    void parseLiteralCharPattern() {
        testSuccessfulParse("case '\\r' -> x", Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 14),
                        literalPatternNode(new Range(0, 5, 0, 9),
                                charNode(new Range(0, 5, 0, 9), '\r')),
                        refNode(new Range(0, 13, 0, 14), "x")));
    }

    @Test
    void parseLiteralStringPattern() {
        testSuccessfulParse("case \"Hello\\n\" -> x", Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 19),
                        literalPatternNode(new Range(0, 5, 0, 14),
                                stringNode(new Range(0, 5, 0, 14), "Hello\n")),
                        refNode(new Range(0, 18, 0, 19), "x")));
    }

    @Test
    void parseLiteralFloatPattern() {
        testSuccessfulParse("case 1.234e+2f -> x", Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 19),
                        literalPatternNode(new Range(0, 5, 0, 14),
                                floatNode(new Range(0, 5, 0, 14), 1.234e+2f)),
                        refNode(new Range(0, 18, 0, 19), "x")));
    }

    @Test
    void parseLiteralDoublePattern() {
        testSuccessfulParse("case 1.234e+2 -> x", Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 18),
                        literalPatternNode(new Range(0, 5, 0, 13),
                                doubleNode(new Range(0, 5, 0, 13), 1.234e+2)),
                        refNode(new Range(0, 17, 0, 18), "x")));
    }

    @Test
    void parseConstructorPattern() {
        testSuccessfulParse("case Cons { head } -> head",
                Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 26),
                        constructorPatternNode(
                                new Range(0, 5, 0, 18),
                                idNode(new Range(0, 5, 0, 9), "Cons"),
                                Lists.immutable.of(
                                        fieldPatternNode(new Range(0, 12, 0, 16), "head",
                                                Optional.empty()))),
                        refNode(new Range(0, 22, 0, 26), "head")));
    }

    @Test
    void parseConstructorPatternAlias() {
        testSuccessfulParse("case cons @ Cons { head } -> head",
                Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 33),
                        aliasPatternNode(new Range(0, 5, 0, 25), "cons",
                                constructorPatternNode(
                                        new Range(0, 12, 0, 25),
                                        idNode(new Range(0, 12, 0, 16), "Cons"),
                                        Lists.immutable.of(
                                                fieldPatternNode(new Range(0, 19, 0, 23), "head",
                                                        Optional.empty())))),
                        refNode(new Range(0, 29, 0, 33), "head")));
    }

    @Test
    void parseConstructorPatternWithFieldPattern() {
        testSuccessfulParse("case Cons { head, tail: Nil {}  } -> head",
                Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 41),
                        constructorPatternNode(
                                new Range(0, 5, 0, 33),
                                idNode(new Range(0, 5, 0, 9), "Cons"),
                                Lists.immutable.of(
                                        fieldPatternNode(new Range(0, 12, 0, 16), "head",
                                                Optional.empty()),
                                        fieldPatternNode(new Range(0, 18, 0, 30), "tail", Optional.of(
                                                constructorPatternNode(
                                                        new Range(0, 24, 0, 30),
                                                        idNode(new Range(0, 24, 0, 27), "Nil"),
                                                        Lists.immutable.empty()))))),
                        refNode(new Range(0, 37, 0, 41), "head")));
    }

    @Test
    void parseConstructorPatternNestedPatternAlias() {
        testSuccessfulParse("case Cons { head, tail: nil @ Nil {}  } -> head",
                Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 47),
                        constructorPatternNode(
                                new Range(0, 5, 0, 39),
                                idNode(new Range(0, 5, 0, 9), "Cons"),
                                Lists.immutable.of(
                                        fieldPatternNode(new Range(0, 12, 0, 16), "head",
                                                Optional.empty()),
                                        fieldPatternNode(new Range(0, 18, 0, 36), "tail", Optional.of(
                                                aliasPatternNode(new Range(0, 24, 0, 36), "nil",
                                                        constructorPatternNode(
                                                                new Range(0, 30, 0, 36),
                                                                idNode(new Range(0, 30, 0, 33), "Nil"),
                                                                Lists.immutable.empty())))))),
                        refNode(new Range(0, 43, 0, 47), "head")));
    }

    // Atomic expressions
    @Test
    void parseLiteralInt() {
        testSuccessfulParse("0", Parser::getExprVisitor, MinaParser::expr,
                intNode(new Range(0, 0, 0, 1), 0));
        testSuccessfulParse("1", Parser::getExprVisitor, MinaParser::expr,
                intNode(new Range(0, 0, 0, 1), 1));
        testSuccessfulParse("1i", Parser::getExprVisitor, MinaParser::expr,
                intNode(new Range(0, 0, 0, 2), 1));
        testSuccessfulParse("1I", Parser::getExprVisitor, MinaParser::expr,
                intNode(new Range(0, 0, 0, 2), 1));
    }

    @Test
    void parseLiteralIntUnderscores() {
        testSuccessfulParse("9_999_999", Parser::getExprVisitor, MinaParser::expr,
                intNode(new Range(0, 0, 0, 9), 9999999));
        testSuccessfulParse("9_999_999i", Parser::getExprVisitor, MinaParser::expr,
                intNode(new Range(0, 0, 0, 10), 9999999));
        testSuccessfulParse("9_999_999I", Parser::getExprVisitor, MinaParser::expr,
                intNode(new Range(0, 0, 0, 10), 9999999));
    }

    @Test
    void parseLiteralIntOverflow() {
        var errors = testFailedParse(Long.toString(1L + Integer.MAX_VALUE), Parser::getExprVisitor,
                MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("Integer overflow detected"));
    }

    @Test
    void parseLiteralLong() {
        testSuccessfulParse("0l", Parser::getExprVisitor, MinaParser::expr,
                longNode(new Range(0, 0, 0, 2), 0L));
        testSuccessfulParse("1l", Parser::getExprVisitor, MinaParser::expr,
                longNode(new Range(0, 0, 0, 2), 1L));
        testSuccessfulParse("1L", Parser::getExprVisitor, MinaParser::expr,
                longNode(new Range(0, 0, 0, 2), 1L));
    }

    @Test
    void parseLiteralLongUnderscores() {
        testSuccessfulParse("9_999_999l", Parser::getExprVisitor, MinaParser::expr,
                longNode(new Range(0, 0, 0, 10), 9999999L));
        testSuccessfulParse("9_999_999L", Parser::getExprVisitor, MinaParser::expr,
                longNode(new Range(0, 0, 0, 10), 9999999L));
    }

    @Test
    void parseLiteralLongOverflow() {
        var errors = testFailedParse(BigDecimal.ONE.add(BigDecimal.valueOf(Long.MAX_VALUE)).toString() + "L",
                Parser::getExprVisitor, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("Long overflow detected"));
    }

    @Test
    void parseLiteralFloat() {
        testSuccessfulParse("123.4f", Parser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 6), 123.4f));
        testSuccessfulParse("123.4F", Parser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 6), 123.4f));
    }

    @Test
    void parseLiteralFloatNoIntegerPart() {
        testSuccessfulParse(".1f", Parser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 3), .1f));
        testSuccessfulParse(".1F", Parser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 3), .1f));
    }

    @Test
    void parseLiteralFloatUnderscores() {
        testSuccessfulParse(".9_999_999f", Parser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 11), 0.9999999f));
        testSuccessfulParse(".9_999_999F", Parser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 11), 0.9999999f));
    }

    @Test
    void parseLiteralFloatPositiveExponent() {
        testSuccessfulParse("1.234e+2f", Parser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 9), 1.234e+2f));
        testSuccessfulParse("1.234e+2F", Parser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 9), 1.234e+2f));
        testSuccessfulParse("1.234E+2f", Parser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 9), 1.234e+2f));
        testSuccessfulParse("1.234E+2F", Parser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 9), 1.234e+2f));
    }

    @Test
    void parseLiteralFloatNegativeExponent() {
        testSuccessfulParse("1.234e-2f", Parser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 9), 1.234e-2f));
        testSuccessfulParse("1.234e-2F", Parser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 9), 1.234e-2f));
        testSuccessfulParse("1.234E-2f", Parser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 9), 1.234e-2f));
        testSuccessfulParse("1.234E-2F", Parser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 9), 1.234e-2f));
    }

    @Test
    void parseLiteralFloatPrecisionLoss() {
        var errors = testFailedParse("1.23456789f", Parser::getExprVisitor,
                MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("Float precision loss detected"));
    }

    @Test
    void parseLiteralDouble() {
        testSuccessfulParse("123.4", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 5), 123.4d));
        testSuccessfulParse("123.4d", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 6), 123.4d));
        testSuccessfulParse("123.4D", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 6), 123.4d));
    }

    @Test
    void parseLiteralDoubleNoIntegerPart() {
        testSuccessfulParse(".1", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 2), .1d));
        testSuccessfulParse(".1d", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 3), .1d));
        testSuccessfulParse(".1D", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 3), .1d));
    }

    @Test
    void parseLiteralDoubleUnderscores() {
        testSuccessfulParse(".9_999_999", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 10), 0.9999999d));
        testSuccessfulParse(".9_999_999d", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 11), 0.9999999d));
        testSuccessfulParse(".9_999_999D", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 11), 0.9999999d));
    }

    @Test
    void parseLiteralDoublePositiveExponent() {
        testSuccessfulParse("1.234e+2", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 8), 1.234e+2d));
        testSuccessfulParse("1.234e+2d", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 9), 1.234e+2d));
        testSuccessfulParse("1.234e+2D", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 9), 1.234e+2d));
        testSuccessfulParse("1.234E+2d", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 9), 1.234e+2d));
        testSuccessfulParse("1.234E+2D", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 9), 1.234e+2d));
    }

    @Test
    void parseLiteralDoubleNegativeExponent() {
        testSuccessfulParse("1.234e-2", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 8), 1.234e-2d));
        testSuccessfulParse("1.234e-2d", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 9), 1.234e-2d));
        testSuccessfulParse("1.234e-2D", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 9), 1.234e-2d));
        testSuccessfulParse("1.234E-2d", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 9), 1.234e-2d));
        testSuccessfulParse("1.234E-2D", Parser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 9), 1.234e-2d));
    }

    @Test
    void parseLiteralDoublePrecisionLoss() {
        var errors = testFailedParse("4.9E-325", Parser::getExprVisitor,
                MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("Double precision loss detected"));
    }

    @Test
    void parseLiteralTrue() {
        testSuccessfulParse("true", Parser::getExprVisitor, MinaParser::expr,
                boolNode(new Range(0, 0, 0, 4), true));
    }

    @Test
    void parseLiteralFalse() {
        testSuccessfulParse("false", Parser::getExprVisitor, MinaParser::expr,
                boolNode(new Range(0, 0, 0, 5), false));
    }

    @Test
    void parseLiteralChar() {
        testSuccessfulParse("'a'", Parser::getExprVisitor, MinaParser::expr,
                charNode(new Range(0, 0, 0, 3), 'a'));
    }

    @Test
    void parseLiteralCharEscape() {
        testSuccessfulParse("'\\n'", Parser::getExprVisitor, MinaParser::expr,
                charNode(new Range(0, 0, 0, 4), '\n'));
    }

    @Test
    void parseLiteralSingleQuoteChar() {
        testSuccessfulParse("'\\\''", Parser::getExprVisitor, MinaParser::expr,
                charNode(new Range(0, 0, 0, 4), '\''));
    }

    @Test
    void parseLiteralBackslashChar() {
        testSuccessfulParse("'\\\\'", Parser::getExprVisitor, MinaParser::expr,
                charNode(new Range(0, 0, 0, 4), '\\'));
    }

    @Test
    void parseLiteralCharUnicodeEscape() {
        testSuccessfulParse("'\\u2022'", Parser::getExprVisitor, MinaParser::expr,
                charNode(new Range(0, 0, 0, 8), '\u2022'));
    }

    @Test
    void parseLiteralString() {
        testSuccessfulParse("\"abc\"", Parser::getExprVisitor, MinaParser::expr,
                stringNode(new Range(0, 0, 0, 5), "abc"));
    }

    @Test
    void parseLiteralStringEscape() {
        testSuccessfulParse("\"Hello\\n\"", Parser::getExprVisitor, MinaParser::expr,
                stringNode(new Range(0, 0, 0, 9), "Hello\n"));
    }

    @Test
    void parseLiteralDoubleQuoteString() {
        testSuccessfulParse("\"\\\"\"", Parser::getExprVisitor, MinaParser::expr,
                stringNode(new Range(0, 0, 0, 4), "\""));
    }

    @Test
    void parseLiteralBackslashString() {
        testSuccessfulParse("\"\\\\\"", Parser::getExprVisitor, MinaParser::expr,
                stringNode(new Range(0, 0, 0, 4), "\\"));
    }

    @Test
    void parseLiteralStringUnicodeEscape() {
        testSuccessfulParse("\"\\u2022 Unicode escape\"", Parser::getExprVisitor, MinaParser::expr,
                stringNode(new Range(0, 0, 0, 23), "\u2022 Unicode escape"));
    }

    @Test
    void parseUnqualifiedId() {
        testSuccessfulParse("foo", Parser::getExprVisitor, MinaParser::expr,
                refNode(new Range(0, 0, 0, 3), "foo"));
    }

    @Test
    void parseQualifiedIdAsSelectionWithoutNamespaceImport() {
        testSuccessfulParse(
            "Parser.compilationUnit", Parser::getExprVisitor, MinaParser::expr,
                selectNode(
                    new Range(0, 0, 0, 22),
                    refNode(new Range(0, 0, 0, 6), "Parser"),
                    refNode(new Range(0, 7, 0, 22), "compilationUnit")));
    }

    @Test
    void parseQualifiedIdWithNamespaceImport() {
        testSuccessfulParse(
            "Parser.compilationUnit", Set.of("Parser"), Parser::getExprVisitor, MinaParser::expr,
            refNode(new Range(0, 0, 0, 22), nsIdNode(new Range(0, 0, 0, 6), "Parser"), "compilationUnit"));
    }

    @Test
    void parseUnfinishedQNInExpression() {
        var errors = testFailedParse("Parser.", Parser::getExprVisitor, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("missing ID at '<EOF>'"));
    }

    @Test
    void parseExprEmptyString() {
        var errors = testFailedParse("", Parser::getExprVisitor, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>'"));
    }
}

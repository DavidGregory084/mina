package org.mina_lang.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mina_lang.syntax.SyntaxNodes.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Test;
import org.mina_lang.common.Range;
import org.mina_lang.parser.Parser.Visitor;
import org.mina_lang.syntax.CompilationUnitNode;
import org.mina_lang.syntax.MetaNode;

public class CompilationUnitParserTest {

    void testSuccessfulParse(
            String source,
            CompilationUnitNode<Void> expected) {
        var errorCollector = new ErrorCollector();
        var actual = new Parser(errorCollector).parse(source);
        assertThat("There should be no parsing errors", errorCollector.getErrors(), empty());
        assertThat("The result syntax node should not be null", actual, notNullValue());
        assertThat(actual, equalTo(expected));
    }

    List<String> testFailedParse(String source) {
        var errorCollector = new ErrorCollector();
        new Parser(errorCollector).parse(source);
        var errors = errorCollector.getErrors();
        assertThat("There should be parsing errors", errors, not(empty()));
        return errors;
    }

    <A extends ParserRuleContext, B extends MetaNode<Void>, C extends Visitor<A, B>> void testSuccessfulParse(
            String source,
            Function<Parser, C> visitor,
            Function<MinaParser, A> startRule,
            B expected) {
        var errorCollector = new ErrorCollector();
        var parser = new Parser(errorCollector);
        var actual = parser.parse(source, visitor, startRule);
        assertThat("There should be no parsing errors", errorCollector.getErrors(), empty());
        assertThat("The result syntax node should not be null", actual, notNullValue());
        assertThat(actual, equalTo(expected));
    }

    <A extends ParserRuleContext, B extends MetaNode<Void>, C extends Visitor<A, B>> List<String> testFailedParse(
            String source,
            Function<Parser, C> visitor,
            Function<MinaParser, A> startRule) {
        var errorCollector = new ErrorCollector();
        var parser = new Parser(errorCollector);
        parser.parse(source, visitor, startRule);
        var errors = errorCollector.getErrors();
        assertThat("There should be parsing errors", errors, not(empty()));
        return errors;
    }

    // Module header
    @Test
    void parseModuleHeader() {
        testSuccessfulParse("module Mina/Test/Parser {}",
                compilationUnitNode(
                        new Range(0, 0, 0, 31),
                        Lists.immutable.of(
                                moduleNode(
                                        new Range(0, 0, 0, 26),
                                        modIdNode(new Range(0, 7, 0, 23), Lists.immutable.of("Mina", "Test"), "Parser"),
                                        Lists.immutable.empty(),
                                        Lists.immutable.empty()))));
    }

    @Test
    void parseModuleHeaderEmptyPackage() {
        testSuccessfulParse("module Parser {}",
                compilationUnitNode(
                        new Range(0, 0, 0, 21),
                        Lists.immutable.of(
                                moduleNode(
                                        new Range(0, 0, 0, 16),
                                        modIdNode(new Range(0, 7, 0, 13), "Parser"),
                                        Lists.immutable.empty(),
                                        Lists.immutable.empty()))));
    }

    @Test
    void parseModuleHeaderMissingName() {
        var errors = testFailedParse("module {}");
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("mismatched input '{' expecting ID"));
    }

    @Test
    void parseModuleHeaderMissingOpenParen() {
        var errors = testFailedParse("module Parser }");
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("missing '{' at '}'"));
    }

    @Test
    void parseModuleHeaderMissingCloseParen() {
        var errors = testFailedParse("module Parser {");
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>'"));
    }

    // Import declarations
    @Test
    void parseImportModuleOnly() {
        testSuccessfulParse("import Mina/Test/Parser", Parser::getImportVisitor,
                MinaParser::importDeclaration,
                importNode(
                        new Range(0, 0, 0, 23),
                        modIdNode(new Range(0, 7, 0, 23), Lists.immutable.of("Mina", "Test"), "Parser")));
    }

    @Test
    void parseImportEmptyPackageModuleOnly() {
        testSuccessfulParse("import Parser", Parser::getImportVisitor, MinaParser::importDeclaration,
                importNode(
                        new Range(0, 0, 0, 13),
                        modIdNode(new Range(0, 7, 0, 13), "Parser")));
    }

    @Test
    void parseImportMultipleSymbols() {
        testSuccessfulParse("import Mina/Test/Parser.{compilationUnit, importDeclaration}",
                Parser::getImportVisitor,
                MinaParser::importDeclaration,
                importNode(
                        new Range(0, 0, 0, 60),
                        modIdNode(new Range(0, 7, 0, 23), Lists.immutable.of("Mina", "Test"), "Parser"),
                        Lists.immutable.of("compilationUnit", "importDeclaration")));
    }

    @Test
    void parseImportEmptyPackageMultipleSymbols() {
        testSuccessfulParse("import Parser.{compilationUnit, importDeclaration}",
                Parser::getImportVisitor,
                MinaParser::importDeclaration,
                importNode(
                        new Range(0, 0, 0, 50),
                        modIdNode(new Range(0, 7, 0, 13), "Parser"),
                        Lists.immutable.of("compilationUnit", "importDeclaration")));
    }

    @Test
    void parseImportSingleSymbol() {
        testSuccessfulParse("import Mina/Test/Parser.ifExpr", Parser::getImportVisitor,
                MinaParser::importDeclaration,
                importNode(
                        new Range(0, 0, 0, 30),
                        modIdNode(new Range(0, 7, 0, 23), Lists.immutable.of("Mina", "Test"), "Parser"),
                        Lists.immutable.of("ifExpr")));
    }

    @Test
    void parseImportEmptyPackageSingleSymbol() {
        testSuccessfulParse("import Parser.ifExpr", Parser::getImportVisitor,
                MinaParser::importDeclaration,
                importNode(
                        new Range(0, 0, 0, 20),
                        modIdNode(new Range(0, 7, 0, 13), "Parser"),
                        Lists.immutable.of("ifExpr")));
    }

    @Test
    void parseImportNoSelector() {
        var errors = testFailedParse("import", Parser::getImportVisitor, MinaParser::importDeclaration);
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>' expecting ID"));
    }

    // Types
    @Test
    void parseIdTypeLambda() {
        testSuccessfulParse("A => A", Parser::getTypeVisitor, MinaParser::type,
                typeLambdaNode(
                        new Range(0, 0, 0, 6),
                        Lists.immutable.of(forAllVarNode(new Range(0, 0, 0, 1), "A")),
                        typeRefNode(new Range(0, 5, 0, 6), "A")));

    }

    @Test
    void parseParenthesizedIdTypeLambda() {
        testSuccessfulParse("[A] => A", Parser::getTypeVisitor, MinaParser::type,
                typeLambdaNode(
                        new Range(0, 0, 0, 8),
                        Lists.immutable.of(forAllVarNode(new Range(0, 1, 0, 2), "A")),
                        typeRefNode(new Range(0, 7, 0, 8), "A")));

    }

    @Test
    void parseTypeLambdaWithGroupingParens() {
        testSuccessfulParse("[B => [A => A]]", Parser::getTypeVisitor, MinaParser::type,
                typeLambdaNode(
                        new Range(0, 1, 0, 14),
                        Lists.immutable.of(forAllVarNode(new Range(0, 1, 0, 2), "B")),
                        typeLambdaNode(
                                new Range(0, 7, 0, 13), Lists.immutable.of(forAllVarNode(new Range(0, 7, 0, 8), "A")),
                                typeRefNode(new Range(0, 12, 0, 13), "A"))));
    }

    @Test
    void parseMultiArgTypeLambda() {
        testSuccessfulParse("[A, B] => A", Parser::getTypeVisitor, MinaParser::type,
                typeLambdaNode(
                        new Range(0, 0, 0, 11),
                        Lists.immutable.of(
                                forAllVarNode(new Range(0, 1, 0, 2), "A"),
                                forAllVarNode(new Range(0, 4, 0, 5), "B")),
                        typeRefNode(new Range(0, 10, 0, 11), "A")));

    }

    @Test
    void parseExistsIdTypeLambda() {
        testSuccessfulParse("?A => ?A", Parser::getTypeVisitor, MinaParser::type,
                typeLambdaNode(
                        new Range(0, 0, 0, 8),
                        Lists.immutable.of(existsVarNode(new Range(0, 0, 0, 2), "?A")),
                        typeRefNode(new Range(0, 6, 0, 8), "?A")));
    }

    @Test
    void parseTypeLambdaMissingBody() {
        var errors = testFailedParse("A =>", Parser::getTypeVisitor, MinaParser::type);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>'"));
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
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>' expecting 'else'"));
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
                                        idPatternNode(new Range(0, 20, 0, 21), Optional.empty(), "y"),
                                        refNode(new Range(0, 25, 0, 26), "z")))));
    }

    @Test
    void parseIdPatternAlias() {
        testSuccessfulParse("case x @ y -> x", Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 15),
                        idPatternNode(new Range(0, 5, 0, 10), Optional.of("x"), "y"),
                        refNode(new Range(0, 14, 0, 15), "x")));
    }

    @Test
    void parseLiteralIntPattern() {
        testSuccessfulParse("case 1 -> x", Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 11),
                        literalPatternNode(new Range(0, 5, 0, 6), Optional.empty(), intNode(new Range(0, 5, 0, 6), 1)),
                        refNode(new Range(0, 10, 0, 11), "x")));
    }

    @Test
    void parseLiteralIntPatternAlias() {
        testSuccessfulParse("case x @ 1 -> x", Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 15),
                        literalPatternNode(new Range(0, 5, 0, 10), Optional.of("x"),
                                intNode(new Range(0, 9, 0, 10), 1)),
                        refNode(new Range(0, 14, 0, 15), "x")));
    }

    @Test
    void parseLiteralLongPattern() {
        testSuccessfulParse("case 9_999_999L -> x", Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 20),
                        literalPatternNode(new Range(0, 5, 0, 15), Optional.empty(),
                                longNode(new Range(0, 5, 0, 15), 9999999L)),
                        refNode(new Range(0, 19, 0, 20), "x")));
    }

    @Test
    void parseLiteralBooleanPattern() {
        testSuccessfulParse("case true -> x", Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 14),
                        literalPatternNode(new Range(0, 5, 0, 9), Optional.empty(),
                                boolNode(new Range(0, 5, 0, 9), true)),
                        refNode(new Range(0, 13, 0, 14), "x")));
    }

    @Test
    void parseLiteralCharPattern() {
        testSuccessfulParse("case '\\r' -> x", Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 14),
                        literalPatternNode(new Range(0, 5, 0, 9), Optional.empty(),
                                charNode(new Range(0, 5, 0, 9), '\r')),
                        refNode(new Range(0, 13, 0, 14), "x")));
    }

    @Test
    void parseLiteralStringPattern() {
        testSuccessfulParse("case \"Hello\\n\" -> x", Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 19),
                        literalPatternNode(new Range(0, 5, 0, 14), Optional.empty(),
                                stringNode(new Range(0, 5, 0, 14), "Hello\n")),
                        refNode(new Range(0, 18, 0, 19), "x")));
    }

    @Test
    void parseLiteralFloatPattern() {
        testSuccessfulParse("case 1.234e+2f -> x", Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 19),
                        literalPatternNode(new Range(0, 5, 0, 14), Optional.empty(),
                                floatNode(new Range(0, 5, 0, 14), 1.234e+2f)),
                        refNode(new Range(0, 18, 0, 19), "x")));
    }

    @Test
    void parseLiteralDoublePattern() {
        testSuccessfulParse("case 1.234e+2 -> x", Parser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 18),
                        literalPatternNode(new Range(0, 5, 0, 13), Optional.empty(),
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
                                Optional.empty(),
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
                        constructorPatternNode(
                                new Range(0, 5, 0, 25),
                                Optional.of("cons"),
                                idNode(new Range(0, 12, 0, 16), "Cons"),
                                Lists.immutable.of(
                                        fieldPatternNode(new Range(0, 19, 0, 23), "head",
                                                Optional.empty()))),
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
                                Optional.empty(),
                                idNode(new Range(0, 5, 0, 9), "Cons"),
                                Lists.immutable.of(
                                        fieldPatternNode(new Range(0, 12, 0, 16), "head",
                                                Optional.empty()),
                                        fieldPatternNode(new Range(0, 18, 0, 30), "tail", Optional.of(
                                                constructorPatternNode(
                                                        new Range(0, 24, 0, 30),
                                                        Optional.empty(),
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
                                Optional.empty(),
                                idNode(new Range(0, 5, 0, 9), "Cons"),
                                Lists.immutable.of(
                                        fieldPatternNode(new Range(0, 12, 0, 16), "head",
                                                Optional.empty()),
                                        fieldPatternNode(new Range(0, 18, 0, 36), "tail", Optional.of(
                                                constructorPatternNode(
                                                        new Range(0, 24, 0, 36),
                                                        Optional.of("nil"),
                                                        idNode(new Range(0, 30, 0, 33), "Nil"),
                                                        Lists.immutable.empty()))))),
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
        System.out.println(BigDecimal.ONE.add(BigDecimal.valueOf(Long.MAX_VALUE)));
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
    void parseQualifiedId() {
        testSuccessfulParse("Parser.compilationUnit", Parser::getExprVisitor, MinaParser::expr,
                refNode(new Range(0, 0, 0, 22), modIdNode(new Range(0, 0, 0, 6), "Parser"), "compilationUnit"));
    }

    @Test
    void parseQNMissingId() {
        var errors = testFailedParse("Parser.", Parser::getExprVisitor, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("missing ID at '<EOF>'"));
    }

    @Test
    void parseFullyQualifiedId() {
        testSuccessfulParse("Mina/Test/Parser.compilationUnit", Parser::getExprVisitor, MinaParser::expr,
                refNode(new Range(0, 0, 0, 32),
                        modIdNode(new Range(0, 0, 0, 16), Lists.immutable.of("Mina", "Test"), "Parser"),
                        "compilationUnit"));
    }

    @Test
    void parseFQNMissingId() {
        var errors = testFailedParse("Mina/Test/Parser.", Parser::getExprVisitor, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("missing ID at '<EOF>'"));
    }

    @Test
    void parsePackageOnly() {
        var errors = testFailedParse("Mina/Test/Parser", Parser::getExprVisitor, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>' expecting '.'"));
    }

    @Test
    void parseExprEmptyString() {
        var errors = testFailedParse("", Parser::getExprVisitor, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>'"));
    }
}

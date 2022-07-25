package org.mina_lang.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.mina_lang.syntax.SyntaxNodes.applyNode;
import static org.mina_lang.syntax.SyntaxNodes.blockExprNode;
import static org.mina_lang.syntax.SyntaxNodes.boolNode;
import static org.mina_lang.syntax.SyntaxNodes.caseNode;
import static org.mina_lang.syntax.SyntaxNodes.charNode;
import static org.mina_lang.syntax.SyntaxNodes.compilationUnitNode;
import static org.mina_lang.syntax.SyntaxNodes.constructorNode;
import static org.mina_lang.syntax.SyntaxNodes.constructorParamNode;
import static org.mina_lang.syntax.SyntaxNodes.constructorPatternNode;
import static org.mina_lang.syntax.SyntaxNodes.dataDeclarationNode;
import static org.mina_lang.syntax.SyntaxNodes.doubleNode;
import static org.mina_lang.syntax.SyntaxNodes.existsVarNode;
import static org.mina_lang.syntax.SyntaxNodes.fieldPatternNode;
import static org.mina_lang.syntax.SyntaxNodes.floatNode;
import static org.mina_lang.syntax.SyntaxNodes.forAllVarNode;
import static org.mina_lang.syntax.SyntaxNodes.funTypeNode;
import static org.mina_lang.syntax.SyntaxNodes.idNode;
import static org.mina_lang.syntax.SyntaxNodes.idPatternNode;
import static org.mina_lang.syntax.SyntaxNodes.ifExprNode;
import static org.mina_lang.syntax.SyntaxNodes.importNode;
import static org.mina_lang.syntax.SyntaxNodes.intNode;
import static org.mina_lang.syntax.SyntaxNodes.lambdaExprNode;
import static org.mina_lang.syntax.SyntaxNodes.letDeclarationNode;
import static org.mina_lang.syntax.SyntaxNodes.letFnDeclarationNode;
import static org.mina_lang.syntax.SyntaxNodes.literalPatternNode;
import static org.mina_lang.syntax.SyntaxNodes.longNode;
import static org.mina_lang.syntax.SyntaxNodes.matchNode;
import static org.mina_lang.syntax.SyntaxNodes.modIdNode;
import static org.mina_lang.syntax.SyntaxNodes.moduleNode;
import static org.mina_lang.syntax.SyntaxNodes.paramNode;
import static org.mina_lang.syntax.SyntaxNodes.refNode;
import static org.mina_lang.syntax.SyntaxNodes.stringNode;
import static org.mina_lang.syntax.SyntaxNodes.typeApplyNode;
import static org.mina_lang.syntax.SyntaxNodes.typeLambdaNode;
import static org.mina_lang.syntax.SyntaxNodes.typeReferenceNode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Test;
import org.mina_lang.common.Range;
import org.mina_lang.parser.CompilationUnitParser.Visitor;
import org.mina_lang.syntax.CompilationUnitNode;
import org.mina_lang.syntax.SyntaxNode;

public class CompilationUnitParserTest {

    void testSuccessfulParse(
            String source,
            CompilationUnitNode<Void> expected) {
        var errorCollector = new ErrorCollector();
        var actual = new CompilationUnitParser(errorCollector).parse(source);
        assertThat("There should be no parsing errors", errorCollector.getErrors(), empty());
        assertThat("The result syntax node should not be null", actual, notNullValue());
        assertThat(actual, equalTo(expected));
    }

    List<String> testFailedParse(String source) {
        var errorCollector = new ErrorCollector();
        new CompilationUnitParser(errorCollector).parse(source);
        var errors = errorCollector.getErrors();
        assertThat("There should be parsing errors", errors, not(empty()));
        return errors;
    }

    <A extends ParserRuleContext, B extends SyntaxNode<Void>, C extends Visitor<A, B>> void testSuccessfulParse(
            String source,
            Function<CompilationUnitParser, C> visitor,
            Function<MinaParser, A> startRule,
            B expected) {
        var errorCollector = new ErrorCollector();
        var parser = new CompilationUnitParser(errorCollector);
        var actual = parser.parse(source, visitor, startRule);
        assertThat("There should be no parsing errors", errorCollector.getErrors(), empty());
        assertThat("The result syntax node should not be null", actual, notNullValue());
        assertThat(actual, equalTo(expected));
    }

    <A extends ParserRuleContext, B extends SyntaxNode<Void>, C extends Visitor<A, B>> List<String> testFailedParse(
            String source,
            Function<CompilationUnitParser, C> visitor,
            Function<MinaParser, A> startRule) {
        var errorCollector = new ErrorCollector();
        var parser = new CompilationUnitParser(errorCollector);
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
        testSuccessfulParse("import Mina/Test/Parser", CompilationUnitParser::getImportVisitor,
                MinaParser::importDeclaration,
                importNode(
                        new Range(0, 0, 0, 23),
                        modIdNode(new Range(0, 7, 0, 23), Lists.immutable.of("Mina", "Test"), "Parser")));
    }

    @Test
    void parseImportEmptyPackageModuleOnly() {
        testSuccessfulParse("import Parser", CompilationUnitParser::getImportVisitor, MinaParser::importDeclaration,
                importNode(
                        new Range(0, 0, 0, 13),
                        modIdNode(new Range(0, 7, 0, 13), "Parser")));
    }

    @Test
    void parseImportMultipleSymbols() {
        testSuccessfulParse("import Mina/Test/Parser.{compilationUnit, importDeclaration}",
                CompilationUnitParser::getImportVisitor,
                MinaParser::importDeclaration,
                importNode(
                        new Range(0, 0, 0, 60),
                        modIdNode(new Range(0, 7, 0, 23), Lists.immutable.of("Mina", "Test"), "Parser"),
                        Lists.immutable.of("compilationUnit", "importDeclaration")));
    }

    @Test
    void parseImportEmptyPackageMultipleSymbols() {
        testSuccessfulParse("import Parser.{compilationUnit, importDeclaration}",
                CompilationUnitParser::getImportVisitor,
                MinaParser::importDeclaration,
                importNode(
                        new Range(0, 0, 0, 50),
                        modIdNode(new Range(0, 7, 0, 13), "Parser"),
                        Lists.immutable.of("compilationUnit", "importDeclaration")));
    }

    @Test
    void parseImportSingleSymbol() {
        testSuccessfulParse("import Mina/Test/Parser.ifExpr", CompilationUnitParser::getImportVisitor,
                MinaParser::importDeclaration,
                importNode(
                        new Range(0, 0, 0, 30),
                        modIdNode(new Range(0, 7, 0, 23), Lists.immutable.of("Mina", "Test"), "Parser"),
                        Lists.immutable.of("ifExpr")));
    }

    @Test
    void parseImportEmptyPackageSingleSymbol() {
        testSuccessfulParse("import Parser.ifExpr", CompilationUnitParser::getImportVisitor,
                MinaParser::importDeclaration,
                importNode(
                        new Range(0, 0, 0, 20),
                        modIdNode(new Range(0, 7, 0, 13), "Parser"),
                        Lists.immutable.of("ifExpr")));
    }

    @Test
    void parseImportNoSelector() {
        var errors = testFailedParse("import", CompilationUnitParser::getImportVisitor, MinaParser::importDeclaration);
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>' expecting ID"));
    }

    // Types
    @Test
    void parseIdTypeLambda() {
        testSuccessfulParse("A => A", CompilationUnitParser::getTypeVisitor, MinaParser::type,
                typeLambdaNode(
                        new Range(0, 0, 0, 6),
                        Lists.immutable.of(forAllVarNode(new Range(0, 0, 0, 1), "A")),
                        typeReferenceNode(new Range(0, 5, 0, 6), "A")));

    }

    @Test
    void parseParenthesizedIdTypeLambda() {
        testSuccessfulParse("[A] => A", CompilationUnitParser::getTypeVisitor, MinaParser::type,
                typeLambdaNode(
                        new Range(0, 0, 0, 8),
                        Lists.immutable.of(forAllVarNode(new Range(0, 1, 0, 2), "A")),
                        typeReferenceNode(new Range(0, 7, 0, 8), "A")));

    }

    @Test
    void parseTypeLambdaWithGroupingParens() {
        testSuccessfulParse("[B => [A => A]]", CompilationUnitParser::getTypeVisitor, MinaParser::type,
                typeLambdaNode(
                        new Range(0, 1, 0, 14),
                        Lists.immutable.of(forAllVarNode(new Range(0, 1, 0, 2), "B")),
                        typeLambdaNode(
                                new Range(0, 7, 0, 13), Lists.immutable.of(forAllVarNode(new Range(0, 7, 0, 8), "A")),
                                typeReferenceNode(new Range(0, 12, 0, 13), "A"))));
    }

    @Test
    void parseMultiArgTypeLambda() {
        testSuccessfulParse("[A, B] => A", CompilationUnitParser::getTypeVisitor, MinaParser::type,
                typeLambdaNode(
                        new Range(0, 0, 0, 11),
                        Lists.immutable.of(
                                forAllVarNode(new Range(0, 1, 0, 2), "A"),
                                forAllVarNode(new Range(0, 4, 0, 5), "B")),
                        typeReferenceNode(new Range(0, 10, 0, 11), "A")));

    }

    @Test
    void parseExistsIdTypeLambda() {
        testSuccessfulParse("?A => ?A", CompilationUnitParser::getTypeVisitor, MinaParser::type,
                typeLambdaNode(
                        new Range(0, 0, 0, 8),
                        Lists.immutable.of(existsVarNode(new Range(0, 0, 0, 2), "?A")),
                        typeReferenceNode(new Range(0, 6, 0, 8), "?A")));
    }

    @Test
    void parseTypeLambdaMissingBody() {
        var errors = testFailedParse("A =>", CompilationUnitParser::getTypeVisitor, MinaParser::type);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>'"));
    }

    @Test
    void parseIdFunType() {
        testSuccessfulParse("A -> A", CompilationUnitParser::getTypeVisitor, MinaParser::type,
                funTypeNode(
                        new Range(0, 0, 0, 6),
                        Lists.immutable.of(typeReferenceNode(new Range(0, 0, 0, 1), "A")),
                        typeReferenceNode(new Range(0, 5, 0, 6), "A")));

    }

    @Test
    void parseParenthesizedIdFunType() {
        testSuccessfulParse("(A) -> A", CompilationUnitParser::getTypeVisitor, MinaParser::type,
                funTypeNode(
                        new Range(0, 0, 0, 8),
                        Lists.immutable.of(typeReferenceNode(new Range(0, 1, 0, 2), "A")),
                        typeReferenceNode(new Range(0, 7, 0, 8), "A")));

    }

    @Test
    void parseNullaryFunType() {
        testSuccessfulParse("() -> A", CompilationUnitParser::getTypeVisitor, MinaParser::type,
                funTypeNode(
                        new Range(0, 0, 0, 7),
                        Lists.immutable.empty(),
                        typeReferenceNode(new Range(0, 6, 0, 7), "A")));

    }

    @Test
    void parseMultiArgFunType() {
        testSuccessfulParse("(A, B) -> A", CompilationUnitParser::getTypeVisitor, MinaParser::type,
                funTypeNode(
                        new Range(0, 0, 0, 11),
                        Lists.immutable.of(
                                typeReferenceNode(new Range(0, 1, 0, 2), "A"),
                                typeReferenceNode(new Range(0, 4, 0, 5), "B")),
                        typeReferenceNode(new Range(0, 10, 0, 11), "A")));

    }

    @Test
    void parseFunTypeMissingReturnType() {
        var errors = testFailedParse("let foo: A -> = bar", CompilationUnitParser::getDeclarationVisitor,
                MinaParser::declaration);
        assertThat(errors, hasSize(2));
        assertThat(errors.get(0), startsWith("extraneous input '='"));
        assertThat(errors.get(1), startsWith("mismatched input '<EOF>'"));
    }

    @Test
    void parseUnaryTypeApplication() {
        testSuccessfulParse("List[Int]", CompilationUnitParser::getTypeVisitor, MinaParser::type,
                typeApplyNode(
                        new Range(0, 0, 0, 9),
                        typeReferenceNode(new Range(0, 0, 0, 4), "List"),
                        Lists.immutable
                                .of(typeReferenceNode(new Range(0, 5, 0, 8), "Int"))));

    }

    @Test
    void parseBinaryTypeApplication() {
        testSuccessfulParse("Either[Error, String]", CompilationUnitParser::getTypeVisitor, MinaParser::type,
                typeApplyNode(
                        new Range(0, 0, 0, 21),
                        typeReferenceNode(new Range(0, 0, 0, 6), "Either"),
                        Lists.immutable.of(
                                typeReferenceNode(new Range(0, 7, 0, 12), "Error"),
                                typeReferenceNode(new Range(0, 14, 0, 20), "String"))));

    }

    @Test
    void parseWildcardTypeApplication() {
        testSuccessfulParse("List[?]", CompilationUnitParser::getTypeVisitor, MinaParser::type,
                typeApplyNode(
                        new Range(0, 0, 0, 7),
                        typeReferenceNode(new Range(0, 0, 0, 4), "List"),
                        Lists.immutable.of(typeReferenceNode(new Range(0, 5, 0, 6), "?"))));

    }

    // Declarations
    @Test
    void parseLetDeclaration() {
        testSuccessfulParse("let x = y", CompilationUnitParser::getDeclarationVisitor, MinaParser::declaration,
                letDeclarationNode(
                        new Range(0, 0, 0, 9),
                        "x",
                        refNode(new Range(0, 8, 0, 9), "y")));
    }

    @Test
    void parseLetDeclarationWithTypeAnnotation() {
        testSuccessfulParse("let x: Int = y", CompilationUnitParser::getDeclarationVisitor, MinaParser::declaration,
                letDeclarationNode(
                        new Range(0, 0, 0, 14),
                        "x",
                        typeReferenceNode(new Range(0, 7, 0, 10), "Int"),
                        refNode(new Range(0, 13, 0, 14), "y")));
    }

    @Test
    void parseLetFnDeclarationWithTypeAnnotation() {
        testSuccessfulParse("let id[A](a: A): A = a", CompilationUnitParser::getDeclarationVisitor,
                MinaParser::declaration,
                letFnDeclarationNode(
                        new Range(0, 0, 0, 22),
                        "id",
                        Lists.immutable.of(forAllVarNode(new Range(0, 7, 0, 8), "A")),
                        Lists.immutable.of(paramNode(new Range(0, 10, 0, 14), "a",
                                typeReferenceNode(new Range(0, 13, 0, 14), "A"))),
                        typeReferenceNode(new Range(0, 17, 0, 18), "A"),
                        refNode(new Range(0, 21, 0, 22), "a")));
    }

    @Test
    void parseEmptyDataDeclaration() {
        testSuccessfulParse("data Void[A] {}", CompilationUnitParser::getDeclarationVisitor, MinaParser::declaration,
                dataDeclarationNode(
                        new Range(0, 0, 0, 15),
                        "Void",
                        Lists.immutable.of(forAllVarNode(new Range(0, 10, 0, 11), "A")),
                        Lists.immutable.empty()));
    }

    @Test
    void parseListDataDeclaration() {
        testSuccessfulParse("data List[A] { case Cons(head: A, tail: List[A]) case Nil() }",
                CompilationUnitParser::getDeclarationVisitor, MinaParser::declaration,
                dataDeclarationNode(
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
                                                        typeReferenceNode(new Range(0, 31, 0, 32), "A")),
                                                // tail: List[A]
                                                constructorParamNode(new Range(0, 34, 0, 47), "tail",
                                                        typeApplyNode(new Range(0, 40, 0, 47),
                                                                typeReferenceNode(new Range(0, 40, 0, 44), "List"),
                                                                Lists.immutable.of(typeReferenceNode(
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
                """, CompilationUnitParser::getDeclarationVisitor, MinaParser::declaration,
                dataDeclarationNode(
                        new Range(0, 0, 3, 1),
                        "Expr",
                        Lists.immutable.of(forAllVarNode(new Range(0, 10, 0, 11), "A")),
                        Lists.immutable.of(
                                // Int
                                constructorNode(new Range(1, 4, 1, 31), "Int",
                                        // i: Int
                                        Lists.immutable.of(constructorParamNode(new Range(1, 13, 1, 19), "i",
                                                typeReferenceNode(new Range(1, 16, 1, 19), "Int"))),
                                        // : Expr[Int]
                                        Optional.of(typeApplyNode(new Range(1, 22, 1, 31),
                                                typeReferenceNode(new Range(1, 22, 1, 26), "Expr"), Lists.immutable
                                                        .of(typeReferenceNode(new Range(1, 27, 1, 30), "Int"))))),
                                // Bool
                                constructorNode(new Range(2, 4, 2, 40), "Bool",
                                        // i: Int
                                        Lists.immutable.of(constructorParamNode(new Range(2, 14, 2, 24), "b",
                                                typeReferenceNode(new Range(2, 17, 2, 24), "Boolean"))),
                                        // : Expr[Int]
                                        Optional.of(typeApplyNode(new Range(2, 27, 2, 40),
                                                typeReferenceNode(new Range(2, 27, 2, 31), "Expr"), Lists.immutable
                                                        .of(typeReferenceNode(new Range(2, 32, 2, 39),
                                                                "Boolean"))))))));
    }

    // Block expressions
    @Test
    void parseBlock() {
        testSuccessfulParse("""
                {
                    foo
                }""", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                blockExprNode(
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
                }""", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                blockExprNode(
                        new Range(0, 0, 3, 1),
                        Lists.immutable.of(
                                letDeclarationNode(new Range(1, 4, 1, 13), "x", intNode(new Range(1, 12, 1, 13), 1))),
                        intNode(new Range(2, 4, 2, 5), 2)));
    }

    @Test
    void parseBlockWithMultipleLet() {
        testSuccessfulParse("""
                {
                    let x = 1
                    let y = 'a'
                    2
                }""", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                blockExprNode(
                        new Range(0, 0, 4, 1),
                        Lists.immutable.of(
                                letDeclarationNode(new Range(1, 4, 1, 13), "x", intNode(new Range(1, 12, 1, 13), 1)),
                                letDeclarationNode(new Range(2, 4, 2, 15), "y",
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
                }""", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                blockExprNode(
                        new Range(0, 0, 6, 1),
                        Lists.immutable.of(
                                letDeclarationNode(new Range(1, 4, 1, 13), "x", intNode(new Range(1, 12, 1, 13), 1)),
                                letDeclarationNode(new Range(2, 4, 4, 5), "y",
                                        blockExprNode(new Range(2, 12, 4, 5), charNode(new Range(3, 8, 3, 11), 'a')))),
                        intNode(new Range(5, 4, 5, 5), 2)));
    }

    // Lambda expressions
    @Test
    void parseNullaryLambda() {
        testSuccessfulParse("() -> 1", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                lambdaExprNode(
                        new Range(0, 0, 0, 7),
                        Lists.immutable.empty(),
                        intNode(new Range(0, 6, 0, 7), 1)));
    }

    @Test
    void parseIdentityLambda() {
        testSuccessfulParse("a -> a", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                lambdaExprNode(
                        new Range(0, 0, 0, 6),
                        Lists.immutable.of(paramNode(new Range(0, 0, 0, 1), "a")),
                        refNode(new Range(0, 5, 0, 6), "a")));
    }

    @Test
    void parseParenthesizedIdentityLambda() {
        testSuccessfulParse("(a) -> a", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                lambdaExprNode(
                        new Range(0, 0, 0, 8),
                        Lists.immutable.of(paramNode(new Range(0, 1, 0, 2), "a")),
                        refNode(new Range(0, 7, 0, 8), "a")));
    }

    @Test
    void parseAnnotatedIdentityLambda() {
        testSuccessfulParse("(a: Int) -> a", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                lambdaExprNode(
                        new Range(0, 0, 0, 13),
                        Lists.immutable.of(
                                paramNode(new Range(0, 1, 0, 7), "a", typeReferenceNode(new Range(0, 4, 0, 7), "Int"))),
                        refNode(new Range(0, 12, 0, 13), "a")));
    }

    @Test
    void parseMultiArgLambda() {
        testSuccessfulParse("(a, b) -> a", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                lambdaExprNode(
                        new Range(0, 0, 0, 11),
                        Lists.immutable.of(
                                paramNode(new Range(0, 1, 0, 2), "a"),
                                paramNode(new Range(0, 4, 0, 5), "b")),
                        refNode(new Range(0, 10, 0, 11), "a")));
    }

    @Test
    void parseAnnotatedMultiArgLambda() {
        testSuccessfulParse("(a: Int, b: Boolean) -> a", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                lambdaExprNode(
                        new Range(0, 0, 0, 25),
                        Lists.immutable.of(
                                paramNode(new Range(0, 1, 0, 7), "a", typeReferenceNode(new Range(0, 4, 0, 7), "Int")),
                                paramNode(new Range(0, 9, 0, 19), "b",
                                        typeReferenceNode(new Range(0, 12, 0, 19), "Boolean"))),
                        refNode(new Range(0, 24, 0, 25), "a")));
    }

    @Test
    void parseLambdaMissingBody() {
        var errors = testFailedParse("a ->", CompilationUnitParser::getExprVisitor, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>'"));
    }

    // If expressions
    @Test
    void parseIfExpression() {
        testSuccessfulParse(
                "if false then 0 else 1", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                ifExprNode(
                        new Range(0, 0, 0, 22),
                        boolNode(new Range(0, 3, 0, 8), false),
                        intNode(new Range(0, 14, 1), 0),
                        intNode(new Range(0, 21, 1), 1)));
    }

    @Test
    void parseIfExpressionMissingCondition() {
        var errors = testFailedParse("if then 0 else 1", CompilationUnitParser::getExprVisitor, MinaParser::expr);
        assertThat(errors.get(0), startsWith("extraneous input 'then'"));
    }

    @Test
    void parseIfExpressionMissingConsequent() {
        var errors = testFailedParse("if false then else 1", CompilationUnitParser::getExprVisitor, MinaParser::expr);
        assertThat(errors.get(0), startsWith("extraneous input 'else'"));
    }

    @Test
    void parseIfExpressionMissingAlternative() {
        var errors = testFailedParse("if false then 0", CompilationUnitParser::getExprVisitor, MinaParser::expr);
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>' expecting 'else'"));
    }

    // Function application
    @Test
    void parseFunctionApplication() {
        testSuccessfulParse("f(x)", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                applyNode(
                        new Range(0, 0, 0, 4),
                        refNode(new Range(0, 0, 0, 1), "f"),
                        Lists.immutable.of(refNode(new Range(0, 2, 0, 3), "x"))));
    }

    @Test
    void parseNullaryFunctionApplication() {
        testSuccessfulParse("f()", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                applyNode(
                        new Range(0, 0, 0, 3),
                        refNode(new Range(0, 0, 0, 1), "f"),
                        Lists.immutable.empty()));
    }

    @Test
    void parseMultiArgFunctionApplication() {
        testSuccessfulParse("f(x, y)", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                applyNode(
                        new Range(0, 0, 0, 7),
                        refNode(new Range(0, 0, 0, 1), "f"),
                        Lists.immutable.of(
                                refNode(new Range(0, 2, 0, 3), "x"),
                                refNode(new Range(0, 5, 0, 6), "y"))));
    }

    @Test
    void parseNestedFunctionApplication() {
        testSuccessfulParse("f(g(x))", CompilationUnitParser::getExprVisitor, MinaParser::expr,
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
        testSuccessfulParse("f(x)(y)", CompilationUnitParser::getExprVisitor, MinaParser::expr,
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
        testSuccessfulParse("f((x)(y))", CompilationUnitParser::getExprVisitor, MinaParser::expr,
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
        testSuccessfulParse("(x -> x)(1)", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                applyNode(
                        new Range(0, 0, 0, 11),
                        lambdaExprNode(
                                new Range(0, 1, 0, 7),
                                Lists.immutable.of(paramNode(new Range(0, 1, 0, 2), "x")),
                                refNode(new Range(0, 6, 0, 7), "x")),
                        Lists.immutable.of(intNode(new Range(0, 9, 0, 10), 1))));
    }

    // Match expressions
    @Test
    void parseNoAltMatchExpression() {
        testSuccessfulParse("match x with {}", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                matchNode(
                        new Range(0, 0, 0, 15),
                        refNode(new Range(0, 6, 0, 7), "x"),
                        Lists.immutable.empty()));
    }

    @Test
    void parseIdPatternMatchExpression() {
        testSuccessfulParse("match x with { case y -> z }", CompilationUnitParser::getExprVisitor, MinaParser::expr,
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
        testSuccessfulParse("case x @ y -> x", CompilationUnitParser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 15),
                        idPatternNode(new Range(0, 5, 0, 10), Optional.of("x"), "y"),
                        refNode(new Range(0, 14, 0, 15), "x")));
    }

    @Test
    void parseLiteralIntPattern() {
        testSuccessfulParse("case 1 -> x", CompilationUnitParser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 11),
                        literalPatternNode(new Range(0, 5, 0, 6), Optional.empty(), intNode(new Range(0, 5, 0, 6), 1)),
                        refNode(new Range(0, 10, 0, 11), "x")));
    }

    @Test
    void parseLiteralIntPatternAlias() {
        testSuccessfulParse("case x @ 1 -> x", CompilationUnitParser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 15),
                        literalPatternNode(new Range(0, 5, 0, 10), Optional.of("x"),
                                intNode(new Range(0, 9, 0, 10), 1)),
                        refNode(new Range(0, 14, 0, 15), "x")));
    }

    @Test
    void parseLiteralLongPattern() {
        testSuccessfulParse("case 9_999_999L -> x", CompilationUnitParser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 20),
                        literalPatternNode(new Range(0, 5, 0, 15), Optional.empty(),
                                longNode(new Range(0, 5, 0, 15), 9999999L)),
                        refNode(new Range(0, 19, 0, 20), "x")));
    }

    @Test
    void parseLiteralBooleanPattern() {
        testSuccessfulParse("case true -> x", CompilationUnitParser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 14),
                        literalPatternNode(new Range(0, 5, 0, 9), Optional.empty(),
                                boolNode(new Range(0, 5, 0, 9), true)),
                        refNode(new Range(0, 13, 0, 14), "x")));
    }

    @Test
    void parseLiteralCharPattern() {
        testSuccessfulParse("case '\\r' -> x", CompilationUnitParser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 14),
                        literalPatternNode(new Range(0, 5, 0, 9), Optional.empty(),
                                charNode(new Range(0, 5, 0, 9), '\r')),
                        refNode(new Range(0, 13, 0, 14), "x")));
    }

    @Test
    void parseLiteralStringPattern() {
        testSuccessfulParse("case \"Hello\\n\" -> x", CompilationUnitParser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 19),
                        literalPatternNode(new Range(0, 5, 0, 14), Optional.empty(),
                                stringNode(new Range(0, 5, 0, 14), "Hello\n")),
                        refNode(new Range(0, 18, 0, 19), "x")));
    }

    @Test
    void parseLiteralFloatPattern() {
        testSuccessfulParse("case 1.234e+2f -> x", CompilationUnitParser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 19),
                        literalPatternNode(new Range(0, 5, 0, 14), Optional.empty(),
                                floatNode(new Range(0, 5, 0, 14), 1.234e+2f)),
                        refNode(new Range(0, 18, 0, 19), "x")));
    }

    @Test
    void parseLiteralDoublePattern() {
        testSuccessfulParse("case 1.234e+2 -> x", CompilationUnitParser::getMatchCaseVisitor, MinaParser::matchCase,
                caseNode(
                        new Range(0, 0, 0, 18),
                        literalPatternNode(new Range(0, 5, 0, 13), Optional.empty(),
                                doubleNode(new Range(0, 5, 0, 13), 1.234e+2)),
                        refNode(new Range(0, 17, 0, 18), "x")));
    }

    @Test
    void parseConstructorPattern() {
        testSuccessfulParse("case Cons { head } -> head",
                CompilationUnitParser::getMatchCaseVisitor, MinaParser::matchCase,
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
                CompilationUnitParser::getMatchCaseVisitor, MinaParser::matchCase,
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
                CompilationUnitParser::getMatchCaseVisitor, MinaParser::matchCase,
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
                CompilationUnitParser::getMatchCaseVisitor, MinaParser::matchCase,
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
        testSuccessfulParse("0", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                intNode(new Range(0, 0, 0, 1), 0));
        testSuccessfulParse("1", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                intNode(new Range(0, 0, 0, 1), 1));
        testSuccessfulParse("1i", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                intNode(new Range(0, 0, 0, 2), 1));
        testSuccessfulParse("1I", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                intNode(new Range(0, 0, 0, 2), 1));
    }

    @Test
    void parseLiteralIntUnderscores() {
        testSuccessfulParse("9_999_999", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                intNode(new Range(0, 0, 0, 9), 9999999));
        testSuccessfulParse("9_999_999i", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                intNode(new Range(0, 0, 0, 10), 9999999));
        testSuccessfulParse("9_999_999I", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                intNode(new Range(0, 0, 0, 10), 9999999));
    }

    @Test
    void parseLiteralIntOverflow() {
        var errors = testFailedParse(Long.toString(1L + Integer.MAX_VALUE), CompilationUnitParser::getExprVisitor,
                MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("Integer overflow detected"));
    }

    @Test
    void parseLiteralLong() {
        testSuccessfulParse("0l", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                longNode(new Range(0, 0, 0, 2), 0L));
        testSuccessfulParse("1l", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                longNode(new Range(0, 0, 0, 2), 1L));
        testSuccessfulParse("1L", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                longNode(new Range(0, 0, 0, 2), 1L));
    }

    @Test
    void parseLiteralLongUnderscores() {
        testSuccessfulParse("9_999_999l", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                longNode(new Range(0, 0, 0, 10), 9999999L));
        testSuccessfulParse("9_999_999L", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                longNode(new Range(0, 0, 0, 10), 9999999L));
    }

    @Test
    void parseLiteralLongOverflow() {
        System.out.println(BigDecimal.ONE.add(BigDecimal.valueOf(Long.MAX_VALUE)));
        var errors = testFailedParse(BigDecimal.ONE.add(BigDecimal.valueOf(Long.MAX_VALUE)).toString() + "L",
                CompilationUnitParser::getExprVisitor, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("Long overflow detected"));
    }

    @Test
    void parseLiteralFloat() {
        testSuccessfulParse("123.4f", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 6), 123.4f));
        testSuccessfulParse("123.4F", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 6), 123.4f));
    }

    @Test
    void parseLiteralFloatNoIntegerPart() {
        testSuccessfulParse(".1f", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 3), .1f));
        testSuccessfulParse(".1F", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 3), .1f));
    }

    @Test
    void parseLiteralFloatUnderscores() {
        testSuccessfulParse(".9_999_999f", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 11), 0.9999999f));
        testSuccessfulParse(".9_999_999F", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 11), 0.9999999f));
    }

    @Test
    void parseLiteralFloatPositiveExponent() {
        testSuccessfulParse("1.234e+2f", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 9), 1.234e+2f));
        testSuccessfulParse("1.234e+2F", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 9), 1.234e+2f));
        testSuccessfulParse("1.234E+2f", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 9), 1.234e+2f));
        testSuccessfulParse("1.234E+2F", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 9), 1.234e+2f));
    }

    @Test
    void parseLiteralFloatNegativeExponent() {
        testSuccessfulParse("1.234e-2f", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 9), 1.234e-2f));
        testSuccessfulParse("1.234e-2F", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 9), 1.234e-2f));
        testSuccessfulParse("1.234E-2f", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 9), 1.234e-2f));
        testSuccessfulParse("1.234E-2F", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                floatNode(new Range(0, 0, 0, 9), 1.234e-2f));
    }

    @Test
    void parseLiteralFloatPrecisionLoss() {
        var errors = testFailedParse("1.23456789f", CompilationUnitParser::getExprVisitor,
                MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("Float precision loss detected"));
    }

    @Test
    void parseLiteralDouble() {
        testSuccessfulParse("123.4", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 5), 123.4d));
        testSuccessfulParse("123.4d", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 6), 123.4d));
        testSuccessfulParse("123.4D", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 6), 123.4d));
    }

    @Test
    void parseLiteralDoubleNoIntegerPart() {
        testSuccessfulParse(".1", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 2), .1d));
        testSuccessfulParse(".1d", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 3), .1d));
        testSuccessfulParse(".1D", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 3), .1d));
    }

    @Test
    void parseLiteralDoubleUnderscores() {
        testSuccessfulParse(".9_999_999", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 10), 0.9999999d));
        testSuccessfulParse(".9_999_999d", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 11), 0.9999999d));
        testSuccessfulParse(".9_999_999D", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 11), 0.9999999d));
    }

    @Test
    void parseLiteralDoublePositiveExponent() {
        testSuccessfulParse("1.234e+2", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 8), 1.234e+2d));
        testSuccessfulParse("1.234e+2d", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 9), 1.234e+2d));
        testSuccessfulParse("1.234e+2D", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 9), 1.234e+2d));
        testSuccessfulParse("1.234E+2d", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 9), 1.234e+2d));
        testSuccessfulParse("1.234E+2D", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 9), 1.234e+2d));
    }

    @Test
    void parseLiteralDoubleNegativeExponent() {
        testSuccessfulParse("1.234e-2", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 8), 1.234e-2d));
        testSuccessfulParse("1.234e-2d", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 9), 1.234e-2d));
        testSuccessfulParse("1.234e-2D", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 9), 1.234e-2d));
        testSuccessfulParse("1.234E-2d", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 9), 1.234e-2d));
        testSuccessfulParse("1.234E-2D", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                doubleNode(new Range(0, 0, 0, 9), 1.234e-2d));
    }

    @Test
    void parseLiteralDoublePrecisionLoss() {
        var errors = testFailedParse("4.9E-325", CompilationUnitParser::getExprVisitor,
                MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("Double precision loss detected"));
    }

    @Test
    void parseLiteralTrue() {
        testSuccessfulParse("true", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                boolNode(new Range(0, 0, 0, 4), true));
    }

    @Test
    void parseLiteralFalse() {
        testSuccessfulParse("false", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                boolNode(new Range(0, 0, 0, 5), false));
    }

    @Test
    void parseLiteralChar() {
        testSuccessfulParse("'a'", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                charNode(new Range(0, 0, 0, 3), 'a'));
    }

    @Test
    void parseLiteralCharEscape() {
        testSuccessfulParse("'\\n'", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                charNode(new Range(0, 0, 0, 4), '\n'));
    }

    @Test
    void parseLiteralSingleQuoteChar() {
        testSuccessfulParse("'\\\''", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                charNode(new Range(0, 0, 0, 4), '\''));
    }

    @Test
    void parseLiteralBackslashChar() {
        testSuccessfulParse("'\\\\'", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                charNode(new Range(0, 0, 0, 4), '\\'));
    }

    @Test
    void parseLiteralCharUnicodeEscape() {
        testSuccessfulParse("'\\u2022'", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                charNode(new Range(0, 0, 0, 8), '\u2022'));
    }

    @Test
    void parseLiteralString() {
        testSuccessfulParse("\"abc\"", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                stringNode(new Range(0, 0, 0, 5), "abc"));
    }

    @Test
    void parseLiteralStringEscape() {
        testSuccessfulParse("\"Hello\\n\"", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                stringNode(new Range(0, 0, 0, 9), "Hello\n"));
    }

    @Test
    void parseLiteralDoubleQuoteString() {
        testSuccessfulParse("\"\\\"\"", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                stringNode(new Range(0, 0, 0, 4), "\""));
    }

    @Test
    void parseLiteralBackslashString() {
        testSuccessfulParse("\"\\\\\"", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                stringNode(new Range(0, 0, 0, 4), "\\"));
    }

    @Test
    void parseLiteralStringUnicodeEscape() {
        testSuccessfulParse("\"\\u2022 Unicode escape\"", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                stringNode(new Range(0, 0, 0, 23), "\u2022 Unicode escape"));
    }

    @Test
    void parseUnqualifiedId() {
        testSuccessfulParse("foo", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                refNode(new Range(0, 0, 0, 3), "foo"));
    }

    @Test
    void parseQualifiedId() {
        testSuccessfulParse("Parser.compilationUnit", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                refNode(new Range(0, 0, 0, 22), modIdNode(new Range(0, 0, 0, 6), "Parser"), "compilationUnit"));
    }

    @Test
    void parseQNMissingId() {
        var errors = testFailedParse("Parser.", CompilationUnitParser::getExprVisitor, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("missing ID at '<EOF>'"));
    }

    @Test
    void parseFullyQualifiedId() {
        testSuccessfulParse("Mina/Test/Parser.compilationUnit", CompilationUnitParser::getExprVisitor, MinaParser::expr,
                refNode(new Range(0, 0, 0, 32),
                        modIdNode(new Range(0, 0, 0, 16), Lists.immutable.of("Mina", "Test"), "Parser"),
                        "compilationUnit"));
    }

    @Test
    void parseFQNMissingId() {
        var errors = testFailedParse("Mina/Test/Parser.", CompilationUnitParser::getExprVisitor, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("missing ID at '<EOF>'"));
    }

    @Test
    void parsePackageOnly() {
        var errors = testFailedParse("Mina/Test/Parser", CompilationUnitParser::getExprVisitor, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>' expecting '.'"));
    }

    @Test
    void parseExprEmptyString() {
        var errors = testFailedParse("", CompilationUnitParser::getExprVisitor, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>'"));
    }
}

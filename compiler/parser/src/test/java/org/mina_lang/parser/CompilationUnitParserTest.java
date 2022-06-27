package org.mina_lang.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.List;
import java.util.function.Function;

import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Test;
import org.mina_lang.common.Range;
import org.mina_lang.parser.CompilationUnitParser.ExprVisitor;
import org.mina_lang.parser.CompilationUnitParser.ImportVisitor;
import org.mina_lang.parser.CompilationUnitParser.Visitor;
import org.mina_lang.syntax.CompilationUnitNode;
import org.mina_lang.syntax.IfExprNode;
import org.mina_lang.syntax.ImportNode;
import org.mina_lang.syntax.LambdaExprNode;
import org.mina_lang.syntax.LiteralBooleanNode;
import org.mina_lang.syntax.LiteralCharNode;
import org.mina_lang.syntax.LiteralIntNode;
import org.mina_lang.syntax.Meta;
import org.mina_lang.syntax.ModuleNode;
import org.mina_lang.syntax.ParamNode;
import org.mina_lang.syntax.QualifiedIdNode;
import org.mina_lang.syntax.ReferenceNode;
import org.mina_lang.syntax.SyntaxNode;

public class CompilationUnitParserTest {

    void testSuccessfulParse(
            String source,
            CompilationUnitNode<Void> expected) {
        var errorCollector = new ErrorCollector();
        var actual = CompilationUnitParser.parse(source, errorCollector);
        assertThat("There should be no parsing errors", errorCollector.getErrors(), empty());
        assertThat("The result syntax node should not be null", actual, notNullValue());
        assertThat(actual, equalTo(expected));
    }

    List<String> testFailedParse(String source) {
        var errorCollector = new ErrorCollector();
        CompilationUnitParser.parse(source, errorCollector);
        var errors = errorCollector.getErrors();
        assertThat("There should be parsing errors", errors, not(empty()));
        return errors;
    }

    <A extends ParserRuleContext, B extends SyntaxNode<Void>, C extends Visitor<A, B>> void testSuccessfulParse(
            String source,
            C visitor,
            Function<MinaParser, A> startRule,
            B expected) {
        var errorCollector = new ErrorCollector();
        var actual = CompilationUnitParser.parse(source, errorCollector, visitor, startRule);
        assertThat("There should be no parsing errors", errorCollector.getErrors(), empty());
        assertThat("The result syntax node should not be null", actual, notNullValue());
        assertThat(actual, equalTo(expected));
    }

    <A extends ParserRuleContext, B extends SyntaxNode<Void>, C extends Visitor<A, B>> List<String> testFailedParse(
            String source,
            C visitor,
            Function<MinaParser, A> startRule) {
        var errorCollector = new ErrorCollector();
        CompilationUnitParser.parse(source, errorCollector, visitor, startRule);
        var errors = errorCollector.getErrors();
        assertThat("There should be parsing errors", errors, not(empty()));
        return errors;
    }

    // Module header
    @Test
    void parseModuleHeader() {
        testSuccessfulParse("module Mina/Test/Parser {}",
                new CompilationUnitNode<>(
                        Meta.empty(new Range(0, 0, 0, 31)),
                        Lists.immutable.of(
                                new ModuleNode<>(
                                        Meta.empty(new Range(0, 0, 0, 26)),
                                        Lists.immutable.of("Mina", "Test"),
                                        "Parser",
                                        Lists.immutable.empty(),
                                        Lists.immutable.empty()))));
    }

    @Test
    void parseModuleHeaderEmptyPackage() {
        testSuccessfulParse("module Parser {}",
                new CompilationUnitNode<>(
                        Meta.empty(new Range(0, 0, 0, 21)),
                        Lists.immutable.of(
                                new ModuleNode<>(
                                        Meta.empty(new Range(0, 0, 0, 16)),
                                        Lists.immutable.empty(),
                                        "Parser",
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
        testSuccessfulParse("import Mina/Test/Parser", ImportVisitor.INSTANCE, MinaParser::importDeclaration,
                new ImportNode<>(
                        Meta.empty(new Range(0, 0, 0, 23)),
                        Lists.immutable.of("Mina", "Test"),
                        "Parser",
                        Lists.immutable.empty()));
    }

    @Test
    void parseImportEmptyPackageModuleOnly() {
        testSuccessfulParse("import Parser", ImportVisitor.INSTANCE, MinaParser::importDeclaration,
                new ImportNode<>(
                        Meta.empty(new Range(0, 0, 0, 13)),
                        Lists.immutable.empty(),
                        "Parser",
                        Lists.immutable.empty()));
    }

    @Test
    void parseImportMultipleSymbols() {
        testSuccessfulParse("import Mina/Test/Parser.{compilationUnit, importDeclaration}", ImportVisitor.INSTANCE,
                MinaParser::importDeclaration,
                new ImportNode<>(
                        Meta.empty(new Range(0, 0, 0, 60)),
                        Lists.immutable.of("Mina", "Test"),
                        "Parser",
                        Lists.immutable.of("compilationUnit", "importDeclaration")));
    }

    @Test
    void parseImportEmptyPackageMultipleSymbols() {
        testSuccessfulParse("import Parser.{compilationUnit, importDeclaration}", ImportVisitor.INSTANCE,
                MinaParser::importDeclaration,
                new ImportNode<>(
                        Meta.empty(new Range(0, 0, 0, 50)),
                        Lists.immutable.empty(),
                        "Parser",
                        Lists.immutable.of("compilationUnit", "importDeclaration")));
    }

    @Test
    void parseImportSingleSymbol() {
        testSuccessfulParse("import Mina/Test/Parser.ifExpr", ImportVisitor.INSTANCE, MinaParser::importDeclaration,
                new ImportNode<>(
                        Meta.empty(new Range(0, 0, 0, 30)),
                        Lists.immutable.of("Mina", "Test"),
                        "Parser",
                        Lists.immutable.of("ifExpr")));
    }

    @Test
    void parseImportEmptyPackageSingleSymbol() {
        testSuccessfulParse("import Parser.ifExpr", ImportVisitor.INSTANCE, MinaParser::importDeclaration,
                new ImportNode<>(
                        Meta.empty(new Range(0, 0, 0, 20)),
                        Lists.immutable.empty(),
                        "Parser",
                        Lists.immutable.of("ifExpr")));
    }

    @Test
    void parseImportNoSelector() {
        var errors = testFailedParse("import", ImportVisitor.INSTANCE, MinaParser::importDeclaration);
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>' expecting ID"));
    }

    // Lambda expressions
    @Test
    void parseNullaryLambda() {
        testSuccessfulParse("() -> 1", ExprVisitor.INSTANCE, MinaParser::expr,
                new LambdaExprNode<>(
                        Meta.empty(new Range(0, 0, 0, 7)),
                        Lists.immutable.empty(),
                        new LiteralIntNode<>(Meta.empty(new Range(0, 6, 0, 7)), 1)));
    }

    @Test
    void parseIdentityLambda() {
        testSuccessfulParse("a -> a", ExprVisitor.INSTANCE, MinaParser::expr,
                new LambdaExprNode<>(
                        Meta.empty(new Range(0, 0, 0, 6)),
                        Lists.immutable.of(new ParamNode<>(Meta.empty(new Range(0, 0, 0, 1)), "a")),
                        new ReferenceNode<>(
                                Meta.empty(new Range(0, 5, 0, 6)),
                                new QualifiedIdNode<>(
                                        Meta.empty(new Range(0, 5, 0, 6)),
                                        Lists.immutable.empty(), "a"))));
    }

    @Test
    void parseParenthesizedIdentityLambda() {
        testSuccessfulParse("(a) -> a", ExprVisitor.INSTANCE, MinaParser::expr,
                new LambdaExprNode<>(
                        Meta.empty(new Range(0, 0, 0, 8)),
                        Lists.immutable.of(new ParamNode<>(Meta.empty(new Range(0, 1, 0, 2)), "a")),
                        new ReferenceNode<>(
                                Meta.empty(new Range(0, 7, 0, 8)),
                                new QualifiedIdNode<>(
                                        Meta.empty(new Range(0, 7, 0, 8)),
                                        Lists.immutable.empty(), "a"))));
    }

    @Test
    void parseMultiArgLambda() {
        testSuccessfulParse("(a, b) -> a", ExprVisitor.INSTANCE, MinaParser::expr,
                new LambdaExprNode<>(
                        Meta.empty(new Range(0, 0, 0, 11)),
                        Lists.immutable.of(
                                new ParamNode<>(Meta.empty(new Range(0, 1, 0, 2)), "a"),
                                new ParamNode<>(Meta.empty(new Range(0, 4, 0, 5)), "b")),
                        new ReferenceNode<>(
                                Meta.empty(new Range(0, 10, 0, 11)),
                                new QualifiedIdNode<>(
                                        Meta.empty(new Range(0, 10, 0, 11)),
                                        Lists.immutable.empty(), "a"))));
    }

    @Test
    void parseLambdaMissingBody() {
        var errors = testFailedParse("a ->", ExprVisitor.INSTANCE, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>'"));
    }

    // If expressions
    @Test
    void parseIfExpression() {
        var condition = new LiteralBooleanNode<>(Meta.empty(new Range(0, 3, 0, 8)), false);
        var consequent = new LiteralIntNode<>(Meta.empty(new Range(0, 14, 1)), 0);
        var alternative = new LiteralIntNode<>(Meta.empty(new Range(0, 21, 1)), 1);
        testSuccessfulParse("if false then 0 else 1", ExprVisitor.INSTANCE, MinaParser::expr,
                new IfExprNode<>(
                        Meta.empty(new Range(0, 0, 0, 22)),
                        condition,
                        consequent,
                        alternative));
    }

    @Test
    void parseIfExpressionMissingCondition() {
        var errors = testFailedParse("if then 0 else 1", ExprVisitor.INSTANCE, MinaParser::expr);
        assertThat(errors.get(0), startsWith("extraneous input 'then'"));
    }

    @Test
    void parseIfExpressionMissingConsequent() {
        var errors = testFailedParse("if false then else 1", ExprVisitor.INSTANCE, MinaParser::expr);
        assertThat(errors.get(0), startsWith("extraneous input 'else'"));
    }

    @Test
    void parseIfExpressionMissingAlternative() {
        var errors = testFailedParse("if false then 0", ExprVisitor.INSTANCE, MinaParser::expr);
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>' expecting 'else'"));
    }

    // Atomic expressions
    @Test
    void parseLiteralInt() {
        testSuccessfulParse("1", ExprVisitor.INSTANCE, MinaParser::expr,
                new LiteralIntNode<>(Meta.empty(new Range(0, 0, 0, 1)), 1));
    }

    @Test
    void parseLiteralTrue() {
        testSuccessfulParse("true", ExprVisitor.INSTANCE, MinaParser::expr,
                new LiteralBooleanNode<>(Meta.empty(new Range(0, 0, 0, 4)), true));
    }

    @Test
    void parseLiteralFalse() {
        testSuccessfulParse("false", ExprVisitor.INSTANCE, MinaParser::expr,
                new LiteralBooleanNode<>(Meta.empty(new Range(0, 0, 0, 5)), false));
    }

    @Test
    void parseLiteralChar() {
        testSuccessfulParse("'a'", ExprVisitor.INSTANCE, MinaParser::expr,
                new LiteralCharNode<>(Meta.empty(new Range(0, 0, 0, 3)), 'a'));
    }

    @Test
    void parseUnqualifiedId() {
        var meta = Meta.empty(new Range(0, 0, 0, 3));
        testSuccessfulParse("foo", ExprVisitor.INSTANCE, MinaParser::expr,
                new ReferenceNode<>(meta, new QualifiedIdNode<>(meta, Lists.immutable.empty(), "foo")));
    }

    @Test
    void parseQualifiedId() {
        var meta = Meta.empty(new Range(0, 0, 0, 22));
        testSuccessfulParse("Parser.compilationUnit", ExprVisitor.INSTANCE, MinaParser::expr,
                new ReferenceNode<>(meta,
                        new QualifiedIdNode<>(meta, Lists.immutable.of("Parser"), "compilationUnit")));
    }

    @Test
    void parseQNMissingId() {
        var errors = testFailedParse("Parser.", ExprVisitor.INSTANCE, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("missing ID at '<EOF>'"));
    }

    @Test
    void parseFullyQualifiedId() {
        var meta = Meta.empty(new Range(0, 0, 0, 32));
        testSuccessfulParse("Mina/Test/Parser.compilationUnit", ExprVisitor.INSTANCE, MinaParser::expr,
                new ReferenceNode<>(meta, new QualifiedIdNode<>(meta,
                        Lists.immutable.of("Mina", "Test", "Parser"), "compilationUnit")));
    }

    @Test
    void parseFQNMissingId() {
        var errors = testFailedParse("Mina/Test/Parser.", ExprVisitor.INSTANCE, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("missing ID at '<EOF>'"));
    }

    @Test
    void parsePackageOnly() {
        var errors = testFailedParse("Mina/Test/Parser", ExprVisitor.INSTANCE, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>' expecting '.'"));
    }

    @Test
    void parseExprEmptyString() {
        var errors = testFailedParse("", ExprVisitor.INSTANCE, MinaParser::expr);
        assertThat(errors, hasSize(1));
        assertThat(errors.get(0), startsWith("mismatched input '<EOF>'"));
    }
}

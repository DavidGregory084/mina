package org.mina_lang.typechecker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mina_lang.syntax.SyntaxNodes.*;

import java.util.List;
import java.util.Map;

import net.jqwik.api.*;
import org.junit.jupiter.api.Test;
import org.mina_lang.common.*;
import org.mina_lang.common.diagnostics.Diagnostic;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.names.Nameless;
import org.mina_lang.common.scopes.BuiltInScope;
import org.mina_lang.common.types.BuiltInType;
import org.mina_lang.common.types.Type;
import org.mina_lang.syntax.DeclarationNode;
import org.mina_lang.syntax.ExprNode;
import org.mina_lang.syntax.NamespaceNode;

public class TypecheckerTest {
    void testSuccessfulTypecheck(
            TypeEnvironment environment,
            NamespaceNode<Name> originalNode,
            NamespaceNode<Attributes> expectedNode) {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, environment);
        var typecheckedNode = typechecker.typecheck(originalNode);
        assertThat(diagnostics.getDiagnostics(), is(empty()));
        assertThat(typecheckedNode, is(equalTo(expectedNode)));
    }

    void testSuccessfulTypecheck(
            TypeEnvironment environment,
            DeclarationNode<Name> originalNode,
            DeclarationNode<Attributes> expectedNode) {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, environment);
        var typecheckedNode = typechecker.typecheck(originalNode);
        assertThat(diagnostics.getDiagnostics(), is(empty()));
        assertThat(typecheckedNode, is(equalTo(expectedNode)));
    }

    void testSuccessfulTypecheck(
            TypeEnvironment environment,
            ExprNode<Name> originalNode,
            ExprNode<Attributes> expectedNode) {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, environment);
        var typecheckedNode = typechecker.typecheck(originalNode);
        assertThat(diagnostics.getDiagnostics(), is(empty()));
        assertThat(typecheckedNode, is(equalTo(expectedNode)));
    }

    ErrorCollector testFailedTypecheck(
            TypeEnvironment environment,
            ExprNode<Name> originalNode) {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, environment);
        typechecker.typecheck(originalNode);
        var errors = diagnostics.getErrors();
        assertThat("There should be type errors", errors, is(not(empty())));
        return diagnostics;
    }

    void assertDiagnostic(List<Diagnostic> diagnostics, Range range, String message) {
        assertThat(diagnostics, is(not(empty())));
        var firstDiagnostic = diagnostics.get(0);
        assertThat(firstDiagnostic.message(), is(equalTo(message)));
        assertThat(firstDiagnostic.range(), is(equalTo(range)));
        assertThat(firstDiagnostic.relatedInformation().toList(), is(empty()));
    }

    @Test
    void typecheckLiteralBoolean() {
        /* true */
        var originalNode = boolNode(Meta.<Name>of(Nameless.INSTANCE), true);
        var expectedNode = boolNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.BOOLEAN)), true);
        testSuccessfulTypecheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void typecheckLiteralChar() {
        /* 'c' */
        var originalNode = charNode(Meta.<Name>of(Nameless.INSTANCE), 'c');
        var expectedNode = charNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.CHAR)), 'c');
        testSuccessfulTypecheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void typecheckLiteralString() {
        /* "foo" */
        var originalNode = stringNode(Meta.<Name>of(Nameless.INSTANCE), "foo");
        var expectedNode = stringNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.STRING)), "foo");
        testSuccessfulTypecheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void typecheckLiteralInt() {
        /* 1 */
        var originalNode = intNode(Meta.<Name>of(Nameless.INSTANCE), 1);
        var expectedNode = intNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.INT)), 1);
        testSuccessfulTypecheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void typecheckLiteralLong() {
        /* 1L */
        var originalNode = longNode(Meta.<Name>of(Nameless.INSTANCE), 1L);
        var expectedNode = longNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.LONG)), 1L);
        testSuccessfulTypecheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void typecheckLiteralFloat() {
        /* 0.1F */
        var originalNode = floatNode(Meta.<Name>of(Nameless.INSTANCE), 0.1F);
        var expectedNode = floatNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.FLOAT)), 0.1F);
        testSuccessfulTypecheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void typecheckLiteralDouble() {
        /* 0.1 */
        var originalNode = doubleNode(Meta.<Name>of(Nameless.INSTANCE), 0.1);
        var expectedNode = doubleNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.DOUBLE)), 0.1);
        testSuccessfulTypecheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void typecheckIf() {
        /* if true then 1 else 2 */
        var originalNode = ifNode(
                Meta.<Name>of(Nameless.INSTANCE),
                boolNode(Meta.<Name>of(Nameless.INSTANCE), true),
                intNode(Meta.<Name>of(Nameless.INSTANCE), 1),
                intNode(Meta.<Name>of(Nameless.INSTANCE), 2));

        var expectedNode = ifNode(
                Meta.of(new Attributes(Nameless.INSTANCE, Type.INT)),
                boolNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.BOOLEAN)), true),
                intNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.INT)), 1),
                intNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.INT)), 2));

        testSuccessfulTypecheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void typecheckIfIllTypedCondition() {
        var condRange = new Range(0, 1, 0, 1);

        /* if "true" then 1 else 2 */
        var originalNode = ifNode(
                Meta.of(Nameless.INSTANCE),
                stringNode(new Meta<Name>(condRange, Nameless.INSTANCE), "true"),
                intNode(Meta.of(Nameless.INSTANCE), 1),
                intNode(Meta.of(Nameless.INSTANCE), 2));

        var collector = testFailedTypecheck(TypeEnvironment.empty(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                condRange,
                "Mismatched type! Expected: Boolean, Actual: String");
    }

    @Test
    void typecheckIfMismatchedBranchTypes() {
        var elseRange = new Range(0, 1, 0, 1);

        /* if true then 1 else "a" */
        var originalNode = ifNode(
                Meta.of(Nameless.INSTANCE),
                boolNode(Meta.of(Nameless.INSTANCE), true),
                intNode(Meta.of(Nameless.INSTANCE), 1),
                stringNode(new Meta<Name>(elseRange, Nameless.INSTANCE), "a"));

        var collector = testFailedTypecheck(TypeEnvironment.empty(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                elseRange,
                "Mismatched type! Expected: Int, Actual: String");
    }

    @Property
    void checkBuiltInSubTypeReflexivity(@ForAll("builtIns") Type builtIn) {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, TypeEnvironment.withBuiltInTypes());
        assertThat(typechecker.checkSubType(builtIn, builtIn), is(true));
    }

    @Provide
    Arbitrary<BuiltInType> builtIns() {
        return Arbitraries.of(Type.builtIns.toSet());
    }
}

/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.typechecker;

import com.opencastsoftware.yvette.Range;
import net.jqwik.api.*;
import net.jqwik.api.Tuple.Tuple2;
import net.jqwik.api.Tuple.Tuple3;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.diagnostics.Diagnostic;
import org.mina_lang.common.diagnostics.NamespaceDiagnosticReporter;
import org.mina_lang.common.names.*;
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mina_lang.syntax.SyntaxNodes.*;

public class TypecheckerTest {
    void testSuccessfulTypecheck(
            TypeEnvironment environment,
            NamespaceNode<Name> originalNode,
            NamespaceNode<Attributes> expectedNode) {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Typechecker.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var typechecker = new Typechecker(scopedCollector, environment);
        var typecheckedNode = typechecker.typecheck(originalNode);
        assertThat(baseCollector.getDiagnostics(), is(empty()));
        assertThat(typecheckedNode, is(equalTo(expectedNode)));
    }

    void testSuccessfulTypecheck(
            TypeEnvironment environment,
            DataNode<Name> originalNode,
            DataNode<Attributes> expectedNode) {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Typechecker.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var typechecker = new Typechecker(scopedCollector, environment);
        var typecheckedNodes = typechecker.typecheck(Lists.immutable.of(originalNode));
        assertThat(baseCollector.getDiagnostics(), is(empty()));
        assertThat(typecheckedNodes.getFirst(), is(equalTo(expectedNode)));
    }

    void testSuccessfulTypecheck(
            TypeEnvironment environment,
            DeclarationNode<Name> originalNode,
            DeclarationNode<Attributes> expectedNode) {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Typechecker.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var typechecker = new Typechecker(scopedCollector, environment);
        var typecheckedNode = typechecker.typecheck(originalNode);
        assertThat(baseCollector.getDiagnostics(), is(empty()));
        assertThat(typecheckedNode, is(equalTo(expectedNode)));
    }

    void testSuccessfulTypecheck(
            TypeEnvironment environment,
            ExprNode<Name> originalNode,
            ExprNode<Attributes> expectedNode) {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Typechecker.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var typechecker = new Typechecker(scopedCollector, environment);
        var typecheckedNode = typechecker.typecheck(originalNode);
        assertThat(baseCollector.getDiagnostics(), is(empty()));
        assertThat(typecheckedNode, is(equalTo(expectedNode)));
    }

    ErrorCollector testFailedTypecheck(
            TypeEnvironment environment,
            DeclarationNode<Name> originalNode) {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Typechecker.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var typechecker = new Typechecker(scopedCollector, environment);
        typechecker.typecheck(originalNode);
        var errors = baseCollector.getErrors();
        assertThat("There should be type errors", errors, is(not(empty())));
        return baseCollector;
    }

    ErrorCollector testFailedTypecheck(
            TypeEnvironment environment,
            ExprNode<Name> originalNode) {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Typechecker.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var typechecker = new Typechecker(scopedCollector, environment);
        typechecker.typecheck(originalNode);
        var errors = baseCollector.getErrors();
        assertThat("There should be type errors", errors, is(not(empty())));
        return baseCollector;
    }

    void assertDiagnostic(List<Diagnostic> diagnostics, Range range, String message) {
        assertThat(diagnostics, is(not(empty())));
        var firstDiagnostic = diagnostics.get(0);
        assertThat(firstDiagnostic.message(), is(equalTo(message)));
        assertThat(firstDiagnostic.location().range(), is(equalTo(range)));
        assertThat(firstDiagnostic.relatedInformation().toList(), is(empty()));
    }

    // Data declarations

    @Test
    @DisplayName("List data type typechecks successfully")
    void typecheckListDataType() {
        var environment = TypeEnvironment.withBuiltInTypes();

        var originalNode = ExampleNodes.List.NAMED_NODE;

        var expectedNode = ExampleNodes.List.KINDED_NODE;

        testSuccessfulTypecheck(environment, originalNode, expectedNode);

        var listLocalEntry = environment.lookupType("List");
        assertThat(listLocalEntry, is(optionalWithValue(ExampleNodes.List.KINDED_META)));
        var listCanonicalEntry = environment.lookupType("Mina/Test/Kindchecker.List");
        assertThat(listCanonicalEntry, is(optionalWithValue(ExampleNodes.List.KINDED_META)));

        var consLocalEntry = environment.lookupValue("Cons");

        assertThat(
                consLocalEntry,
                is(optionalWithValue(Meta.of(ExampleNodes.Cons.NAME, ExampleNodes.Cons.TYPE))));

        var consCanonicalEntry = environment.lookupValue("Mina/Test/Kindchecker.Cons");

        assertThat(
                consCanonicalEntry,
                is(optionalWithValue(Meta.of(ExampleNodes.Cons.NAME, ExampleNodes.Cons.TYPE))));

        var nilLocalEntry = environment.lookupValue("Nil");

        assertThat(
                nilLocalEntry,
                is(optionalWithValue(Meta.of(ExampleNodes.Nil.NAME, ExampleNodes.Nil.TYPE))));

        var nilCanonicalEntry = environment.lookupValue("Mina/Test/Kindchecker.Nil");

        assertThat(
                nilCanonicalEntry,
                is(optionalWithValue(Meta.of(ExampleNodes.Nil.NAME, ExampleNodes.Nil.TYPE))));
    }

    @Test
    @DisplayName("Fix data type typechecks successfully")
    void typecheckFixDataType() {
        var environment = TypeEnvironment.withBuiltInTypes();

        var originalNode = ExampleNodes.Fix.NAMED_NODE;

        var expectedNode = ExampleNodes.Fix.KINDED_NODE;

        testSuccessfulTypecheck(environment, originalNode, expectedNode);

        var fixLocalEntry = environment.lookupType("Fix");
        assertThat(fixLocalEntry, is(optionalWithValue(ExampleNodes.Fix.KINDED_META)));
        var fixCanonicalEntry = environment.lookupType("Mina/Test/Kindchecker.Fix");
        assertThat(fixCanonicalEntry, is(optionalWithValue(ExampleNodes.Fix.KINDED_META)));

        var unfixLocalEntry = environment.lookupValue("Unfix");

        assertThat(
                unfixLocalEntry,
                is(optionalWithValue(Meta.of(ExampleNodes.Unfix.NAME, ExampleNodes.Unfix.TYPE))));

        var unfixCanonicalEntry = environment.lookupValue("Mina/Test/Kindchecker.Unfix");

        assertThat(
                unfixCanonicalEntry,
                is(optionalWithValue(Meta.of(ExampleNodes.Unfix.NAME, ExampleNodes.Unfix.TYPE))));
    }

    @Provide
    Arbitrary<Tuple2<LiteralNode<Name>, BuiltInType>> literals() {
        return Arbitraries.oneOf(
                Arbitraries.of(true, false)
                        .map(b -> Tuple.of(boolNode(Meta.of(Nameless.INSTANCE), b),
                                Type.BOOLEAN)),
                Arbitraries.chars()
                        .map(c -> Tuple.of(charNode(Meta.of(Nameless.INSTANCE), c), Type.CHAR)),
                Arbitraries.strings().ofMaxLength(100)
                        .map(s -> Tuple.of(stringNode(Meta.of(Nameless.INSTANCE), s),
                                Type.STRING)),
                Arbitraries.integers()
                        .map(i -> Tuple.of(intNode(Meta.of(Nameless.INSTANCE), i), Type.INT)),
                Arbitraries.longs()
                        .map(l -> Tuple.of(longNode(Meta.of(Nameless.INSTANCE), l), Type.LONG)),
                Arbitraries.floats()
                        .map(f -> Tuple.of(floatNode(Meta.of(Nameless.INSTANCE), f),
                                Type.FLOAT)),
                Arbitraries.doubles()
                        .map(d -> Tuple.of(doubleNode(Meta.of(Nameless.INSTANCE), d),
                                Type.DOUBLE)));
    }

    // Let declarations

    @Property
    @Label("Correctly annotated let bindings to literals typecheck successfully")
    void typecheckAnnotatedLiteralLetBinding(@ForAll("literals") Tuple2<LiteralNode<Name>, BuiltInType> tuple) {
        var originalLiteralNode = tuple.get1();
        var expectedType = tuple.get2();

        var expectedLiteralNode = originalLiteralNode
                .accept(new LiteralNodeMetaTransformer<Name, Attributes>() {
                    @Override
                    public Meta<Attributes> updateMeta(Meta<Name> meta) {
                        return meta.withMeta(new Attributes(meta.meta(), expectedType));
                    }
                });

        var letName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "testLiteral"));

        var builtInName = new BuiltInName(expectedType.name());

        var originalNode = letNode(
                Meta.of(letName),
                "testLiteral",
                typeRefNode(Meta.of(builtInName), expectedType.name()),
                originalLiteralNode);

        var expectedNode = letNode(
                Meta.of(letName, expectedType),
                "testLiteral",
                typeRefNode(
                        Meta.of(builtInName, TypeKind.INSTANCE),
                        expectedType.name()),
                expectedLiteralNode);

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("Correctly annotated let binding to empty block typechecks successfully")
    void typecheckAnnotatedEmptyBlockLetBinding() {
        var letName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "testEmptyBlock"));

        var builtInName = new BuiltInName("Unit");

        var originalNode = letNode(
                Meta.of(letName),
                "testEmptyBlock",
                typeRefNode(Meta.of(builtInName), "Unit"),
                blockNode(
                        ExampleNodes.namelessMeta(),
                        Optional.empty()));

        var expectedNode = letNode(
                Meta.of(letName, Type.UNIT),
                "testEmptyBlock",
                typeRefNode(Meta.of(builtInName, TypeKind.INSTANCE), "Unit"),
                blockNode(
                        ExampleNodes.namelessMeta(Type.UNIT),
                        Optional.empty()));

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("Incorrectly annotated let binding to empty block fails to typecheck")
    void typecheckIllTypedAnnotatedEmptyBlockLetBinding() {
        var emptyBlockRange = new Range(0, 1, 0, 1);

        var letName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "testEmptyBlock"));

        var builtInName = new BuiltInName("Int");

        var originalNode = letNode(
                Meta.of(letName),
                "testEmptyBlock",
                typeRefNode(Meta.of(builtInName), "Int"),
                blockNode(
                        new Meta<>(emptyBlockRange, Nameless.INSTANCE),
                        Optional.empty()));

        var collector = testFailedTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                emptyBlockRange,
                "Mismatched type! Expected: Int, Actual: Unit");
    }

    @Test
    @DisplayName("Correctly annotated let binding to block with result typechecks successfully")
    void typecheckAnnotatedBlockLetBinding() {
        var letName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "testBlock"));

        var builtInName = new BuiltInName("Int");

        /*- let  */
        var originalNode = letNode(
                Meta.of(letName),
                "testBlock",
                typeRefNode(Meta.of(builtInName), "Int"),
                blockNode(
                        ExampleNodes.namelessMeta(),
                        Optional.of(ExampleNodes.Int.namedNode(1))));

        var expectedNode = letNode(
                Meta.of(letName, Type.INT),
                "testBlock",
                typeRefNode(Meta.of(builtInName, TypeKind.INSTANCE), "Int"),
                blockNode(
                        ExampleNodes.namelessMeta(Type.INT),
                        Optional.of(ExampleNodes.Int.typedNode(1))));

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("Incorrectly annotated let binding to block with result fails to typecheck")
    void typecheckIllTypedAnnotatedBlockLetBinding() {
        var intRange = new Range(0, 1, 0, 1);

        var letName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "testBlock"));

        var builtInName = new BuiltInName("Unit");

        var originalNode = letNode(
                Meta.of(letName),
                "testBlock",
                typeRefNode(Meta.of(builtInName), "Unit"),
                blockNode(
                        ExampleNodes.namelessMeta(),
                        Optional.of(intNode(new Meta<>(intRange, Nameless.INSTANCE), 1))));

        var collector = testFailedTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                intRange,
                "Mismatched type! Expected: Unit, Actual: Int");
    }

    @Property
    @Label("Unannotated let bindings to literals typecheck successfully")
    void typecheckUnannotatedLiteralLetBinding(@ForAll("literals") Tuple2<LiteralNode<Name>, BuiltInType> tuple) {
        var originalLiteralNode = tuple.get1();
        var expectedType = tuple.get2();

        var expectedLiteralNode = originalLiteralNode
                .accept(new LiteralNodeMetaTransformer<Name, Attributes>() {
                    @Override
                    public Meta<Attributes> updateMeta(Meta<Name> meta) {
                        return meta.withMeta(new Attributes(meta.meta(), expectedType));
                    }
                });

        var letName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "testLiteral"));

        var originalNode = letNode(
                Meta.of(letName),
                "testLiteral",
                originalLiteralNode);

        var expectedNode = letNode(
                Meta.of(letName, expectedType),
                "testLiteral",
                expectedLiteralNode);

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Provide
    Arbitrary<Tuple3<LiteralNode<Name>, BuiltInType, BuiltInType>> illTypedLiterals() {
        return Arbitraries.lazy(() -> literals().flatMap(tuple -> {
            return builtIns()
                    .filter(builtIn -> !builtIn.equals(tuple.get2()))
                    .map(builtIn -> Tuple.of(tuple.get1(), tuple.get2(), builtIn));
        }));

    }

    @Property
    @Label("Incorrectly annotated let bindings to literals fail to typecheck")
    void typecheckWrongLetAnnotation(
            @ForAll("illTypedLiterals") Tuple3<LiteralNode<Name>, BuiltInType, BuiltInType> tuple) {
        var originalLiteralNode = tuple.get1();
        var actualLiteralType = tuple.get2();
        var incorrectExpectedType = tuple.get3();

        var letName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "testLiteral"));

        var builtInName = new BuiltInName(incorrectExpectedType.name());

        var originalNode = letNode(
                Meta.of(letName),
                "testLiteral",
                typeRefNode(Meta.of(builtInName), incorrectExpectedType.name()),
                originalLiteralNode);

        var collector = testFailedTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                originalLiteralNode.range(),
                "Mismatched type! Expected: " + incorrectExpectedType.name() +
                        ", Actual: " + actualLiteralType.name());
    }

    @Test
    @DisplayName("Correctly annotated let bindings to variables in the environment typecheck successfully")
    void typecheckAnnotatedLetBoundReference() {
        var environment = TypeEnvironment.withBuiltInTypes();
        environment.putType("Bool", ExampleNodes.Bool.KINDED_META);
        environment.putValue("True", ExampleNodes.True.TYPED_META);
        environment.putValue("False", ExampleNodes.False.TYPED_META);

        var originalBoolNode = refNode(Meta.<Name>of(ExampleNodes.True.NAME), "True");
        var expectedBoolNode = refNode(ExampleNodes.True.TYPED_META, "True");

        var letName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "testBool"));

        /*- let testBool: () -> Bool = True */
        var originalNode = letNode(
                Meta.<Name>of(letName),
                "testBool",
                ExampleNodes.True.NAMED_TYPE_NODE,
                originalBoolNode);

        var expectedNode = letNode(
                Meta.of(letName, ExampleNodes.True.TYPE),
                "testBool",
                ExampleNodes.True.KINDED_TYPE_NODE,
                expectedBoolNode);

        testSuccessfulTypecheck(environment, originalNode, expectedNode);
    }

    @Test
    @DisplayName("Incorrectly annotated let bindings to variables in the environment fail to typecheck")
    void typecheckIllTypedLetBoundReference() {
        var environment = TypeEnvironment.withBuiltInTypes();
        environment.putType("Bool", ExampleNodes.Bool.KINDED_META);
        environment.putValue("True", ExampleNodes.True.TYPED_META);
        environment.putValue("False", ExampleNodes.False.TYPED_META);

        var boolReferenceRange = new Range(0, 1, 0, 1);
        var originalBoolNode = refNode(new Meta<Name>(boolReferenceRange, ExampleNodes.True.NAME), "True");

        var letName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "testBool"));

        var illTypedBoolConstrTypeNode = funTypeNode(
                Meta.of(Nameless.INSTANCE),
                Lists.immutable.of(typeRefNode(Meta.of(new BuiltInName("Int")), "Int")),
                typeRefNode(Meta.of(ExampleNodes.Bool.NAME), "Bool"));

        /*- let testBool: Int -> Bool = True */
        var originalNode = letNode(
                Meta.<Name>of(letName),
                "testBool",
                illTypedBoolConstrTypeNode,
                originalBoolNode);

        var collector = testFailedTypecheck(environment, originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                boolReferenceRange,
                "Mismatched type! Expected: Int -> Bool, Actual: () -> Bool");
    }

    @Test
    @DisplayName("Unannotated let bindings to variables in the environment typecheck successfully")
    void typecheckUnannotatedLetBoundReference() {
        var environment = TypeEnvironment.withBuiltInTypes();
        environment.putType("Bool", ExampleNodes.Bool.KINDED_META);
        environment.putValue("True", ExampleNodes.True.TYPED_META);
        environment.putValue("False", ExampleNodes.False.TYPED_META);

        var originalBoolNode = refNode(Meta.<Name>of(ExampleNodes.True.NAME), "True");
        var expectedBoolNode = refNode(ExampleNodes.True.TYPED_META, "True");

        var letName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "testBool"));

        /*- let testBool = True */
        var originalNode = letNode(
                Meta.of(letName),
                "testBool",
                originalBoolNode);

        var expectedNode = letNode(
                Meta.of(letName, ExampleNodes.True.TYPE),
                "testBool",
                expectedBoolNode);

        testSuccessfulTypecheck(environment, originalNode, expectedNode);
    }

    @Test
    @DisplayName("Annotated let bound lambdas typecheck successfully without lambda parameter annotations")
    void typecheckAnnotatedLetLambdaParams() {
        var letName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "testAnnotatedLambda"));

        /*- let testAnnotatedLambda: Int -> Int = i -> i */
        var originalNode = letNode(
                Meta.of(letName),
                "testAnnotatedLambda",
                funTypeNode(
                        Meta.of(Nameless.INSTANCE),
                        Lists.immutable.of(typeRefNode(Meta.of(new BuiltInName("Int")), "Int")),
                        typeRefNode(Meta.of(new BuiltInName("Int")), "Int")),
                lambdaNode(
                        Meta.of(Nameless.INSTANCE),
                        Lists.immutable.of(paramNode(Meta.of(new LocalName("i", 0)), "i")),
                        refNode(Meta.of(new LocalName("i", 0)), "i")));

        var expectedNode = letNode(
                Meta.of(letName, Type.function(Type.INT, Type.INT)),
                "testAnnotatedLambda",
                funTypeNode(
                        Meta.nameless(TypeKind.INSTANCE),
                        Lists.immutable.of(typeRefNode(Meta.of(new BuiltInName("Int"), TypeKind.INSTANCE), "Int")),
                        typeRefNode(Meta.of(new BuiltInName("Int"), TypeKind.INSTANCE), "Int")),
                lambdaNode(
                        Meta.nameless(Type.function(Type.INT, Type.INT)),
                        Lists.immutable.of(paramNode(Meta.of(new LocalName("i", 0), Type.INT), "i")),
                        refNode(Meta.of(new LocalName("i", 0), Type.INT), "i")));

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("Annotated let bound lambda with function argument typechecks successfully without lambda parameter annotations")
    void typecheckAnnotatedLetLambdaFunctionParam() {
        var letName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "testAnnotatedLambda"));

        /*- let testAnnotatedLambda: (Int -> Int) -> Int = f -> f(1) */
        var originalNode = letNode(
                Meta.of(letName),
                "testAnnotatedLambda",
                /*- (Int -> Int) -> Int */
                funTypeNode(
                        ExampleNodes.namelessMeta(),
                        Lists.immutable.of(
                                funTypeNode(
                                        ExampleNodes.namelessMeta(),
                                        Lists.immutable.of(
                                                ExampleNodes.Int.NAMED_TYPE_NODE),
                                        ExampleNodes.Int.NAMED_TYPE_NODE)),
                        ExampleNodes.Int.NAMED_TYPE_NODE),
                /*- f -> f(1) */
                lambdaNode(
                        ExampleNodes.namelessMeta(),
                        Lists.immutable.of(ExampleNodes.Param.namedNode("f")),
                        applyNode(
                                ExampleNodes.namelessMeta(),
                                ExampleNodes.LocalVar.namedNode("f"),
                                Lists.immutable.of(ExampleNodes.Int.namedNode(1)))));

        var intToInt = Type.function(Type.INT, Type.INT);
        var intToIntToInt = Type.function(intToInt, Type.INT);

        var expectedNode = letNode(
                Meta.of(letName, intToIntToInt),
                "testAnnotatedLambda",
                funTypeNode(
                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                        Lists.immutable.of(
                                funTypeNode(
                                        ExampleNodes.namelessMeta(
                                                TypeKind.INSTANCE),
                                        Lists.immutable.of(
                                                ExampleNodes.Int.KINDED_TYPE_NODE),
                                        ExampleNodes.Int.KINDED_TYPE_NODE)),
                        ExampleNodes.Int.KINDED_TYPE_NODE),
                lambdaNode(
                        ExampleNodes.namelessMeta(intToIntToInt),
                        Lists.immutable.of(ExampleNodes.Param.typedNode("f", intToInt)),
                        applyNode(
                                ExampleNodes.namelessMeta(Type.INT),
                                ExampleNodes.LocalVar.typedNode("f", intToInt),
                                Lists.immutable.of(ExampleNodes.Int.typedNode(1)))));

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("Annotated polymorphic identity function typechecks successfully")
    void typecheckAnnotatedPolyIdLet() {
        var tyVarA = new ForAllVar("A", TypeKind.INSTANCE);
        var tyVarAName = new ForAllVarName("A");
        var tyVarAMeta = Meta.of(tyVarAName, TypeKind.INSTANCE);

        var letName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "id"));

        var letType = new QuantifiedType(
                Lists.immutable.of(tyVarA),
                Type.function(tyVarA, tyVarA),
                TypeKind.INSTANCE);

        var letMeta = Meta.of(letName, letType);

        /*- let id: [A] { A -> A } = a -> a  */
        var originalNode = letNode(
                Meta.of(letName),
                "id",
                quantifiedTypeNode(
                        ExampleNodes.namelessMeta(),
                        Lists.immutable.of(forAllVarNode(Meta.of(tyVarAName), "A")),
                        funTypeNode(
                                ExampleNodes.namelessMeta(),
                                Lists.immutable.of(typeRefNode(Meta.of(tyVarAName), "A")),
                                typeRefNode(Meta.of(tyVarAName), "A"))),
                lambdaNode(
                        Meta.of(Nameless.INSTANCE),
                        Lists.immutable.of(ExampleNodes.Param.namedNode("a")),
                        ExampleNodes.LocalVar.namedNode("a")));

        var expectedNode = letNode(
                letMeta,
                "id",
                quantifiedTypeNode(
                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                        Lists.immutable.of(forAllVarNode(tyVarAMeta, "A")),
                        funTypeNode(
                                ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                                Lists.immutable.of(typeRefNode(tyVarAMeta, "A")),
                                typeRefNode(tyVarAMeta, "A"))),
                lambdaNode(
                        ExampleNodes.namelessMeta(letType.body()),
                        Lists.immutable.of(ExampleNodes.Param.typedNode("a", tyVarA)),
                        ExampleNodes.LocalVar.typedNode("a", tyVarA)));

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("Annotated polymorphic const function typechecks successfully")
    void typecheckAnnotatedPolyConstLet() {
        var tyVarA = new ForAllVar("A", TypeKind.INSTANCE);
        var tyVarAName = new ForAllVarName("A");
        var tyVarAMeta = Meta.of(tyVarAName, TypeKind.INSTANCE);

        var tyVarB = new ForAllVar("B", TypeKind.INSTANCE);
        var tyVarBName = new ForAllVarName("B");
        var tyVarBMeta = Meta.of(tyVarBName, TypeKind.INSTANCE);

        var letName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "const"));

        var letType = new QuantifiedType(
                Lists.immutable.of(tyVarA, tyVarB),
                Type.function(tyVarA, tyVarB, tyVarA),
                TypeKind.INSTANCE);

        var letMeta = Meta.of(letName, letType);

        /*- let const: [A, B] { (A, B) -> A } = (a, b) -> a  */
        var originalNode = letNode(
                Meta.of(letName),
                "const",
                quantifiedTypeNode(
                        ExampleNodes.namelessMeta(),
                        Lists.immutable.of(
                                forAllVarNode(Meta.of(tyVarAName), "A"),
                                forAllVarNode(Meta.of(tyVarBName), "B")),
                        funTypeNode(
                                ExampleNodes.namelessMeta(),
                                Lists.immutable.of(
                                        typeRefNode(Meta.of(tyVarAName), "A"),
                                        typeRefNode(Meta.of(tyVarBName), "B")),
                                typeRefNode(Meta.of(tyVarAName), "A"))),
                lambdaNode(
                        Meta.of(Nameless.INSTANCE),
                        Lists.immutable.of(
                                ExampleNodes.Param.namedNode("a"),
                                ExampleNodes.Param.namedNode("b")),
                        ExampleNodes.LocalVar.namedNode("a")));

        var expectedNode = letNode(
                letMeta,
                "const",
                quantifiedTypeNode(
                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                        Lists.immutable.of(
                                forAllVarNode(tyVarAMeta, "A"),
                                forAllVarNode(tyVarBMeta, "B")),
                        funTypeNode(
                                ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                                Lists.immutable.of(
                                        typeRefNode(tyVarAMeta, "A"),
                                        typeRefNode(tyVarBMeta, "B")),
                                typeRefNode(tyVarAMeta, "A"))),
                lambdaNode(
                        ExampleNodes.namelessMeta(letType.body()),
                        Lists.immutable.of(
                                ExampleNodes.Param.typedNode("a", tyVarA),
                                ExampleNodes.Param.typedNode("b", tyVarB)),
                        ExampleNodes.LocalVar.typedNode("a", tyVarA)));

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("Annotated letfn with function argument typechecks successfully without parameter annotations")
    void typecheckAnnotatedLetFnLambdaFunctionParam() {
        var letName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "testAnnotatedLambda"));

        /*- let testAnnotatedLambda(f): Int = f(1) */
        var originalNode = letFnNode(
                Meta.of(letName),
                "testAnnotatedLambda",
                Lists.immutable.of(ExampleNodes.Param.namedNode("f")),
                ExampleNodes.Int.NAMED_TYPE_NODE,
                applyNode(
                        ExampleNodes.namelessMeta(),
                        ExampleNodes.LocalVar.namedNode("f"),
                        Lists.immutable.of(ExampleNodes.Int.namedNode(1))));

        var intToInt = Type.function(Type.INT, Type.INT);
        var intToIntToInt = Type.function(intToInt, Type.INT);

        var expectedNode = letFnNode(
                Meta.of(letName, intToIntToInt),
                "testAnnotatedLambda",
                Lists.immutable.of(ExampleNodes.Param.typedNode("f", intToInt)),
                ExampleNodes.Int.KINDED_TYPE_NODE,
                applyNode(
                        ExampleNodes.namelessMeta(Type.INT),
                        ExampleNodes.LocalVar.typedNode("f", intToInt),
                        Lists.immutable.of(ExampleNodes.Int.typedNode(1))));

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("Annotated polymorphic identity letfn typechecks successfully")
    void typecheckAnnotatedPolyIdLetFn() {
        var tyVarA = new ForAllVar("A", TypeKind.INSTANCE);
        var tyVarAName = new ForAllVarName("A");
        var tyVarAMeta = Meta.of(tyVarAName, TypeKind.INSTANCE);

        var letName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "id"));

        var letType = new QuantifiedType(
                Lists.immutable.of(tyVarA),
                Type.function(tyVarA, tyVarA),
                new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE));

        var letMeta = Meta.of(letName, letType);

        /*- let id[A](a: A): A = a  */
        var originalNode = letFnNode(
                Meta.of(letName),
                "id",
                Lists.immutable.of(forAllVarNode(Meta.of(tyVarAName), "A")),
                Lists.immutable.of(ExampleNodes.Param.namedNode("a", typeRefNode(Meta.of(tyVarAName), "A"))),
                typeRefNode(Meta.of(tyVarAName), "A"),
                ExampleNodes.LocalVar.namedNode("a"));

        var expectedNode = letFnNode(
                letMeta,
                "id",
                Lists.immutable.of(forAllVarNode(tyVarAMeta, "A")),
                Lists.immutable.of(ExampleNodes.Param.typedNode("a", tyVarA, typeRefNode(tyVarAMeta, "A"))),
                typeRefNode(tyVarAMeta, "A"),
                ExampleNodes.LocalVar.typedNode("a", tyVarA));

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("Annotated polymorphic const letfn typechecks successfully")
    void typecheckAnnotatedPolyConstLetFn() {
        var tyVarA = new ForAllVar("A", TypeKind.INSTANCE);
        var tyVarAName = new ForAllVarName("A");
        var tyVarAMeta = Meta.of(tyVarAName, TypeKind.INSTANCE);

        var tyVarB = new ForAllVar("B", TypeKind.INSTANCE);
        var tyVarBName = new ForAllVarName("B");
        var tyVarBMeta = Meta.of(tyVarBName, TypeKind.INSTANCE);

        var letName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "const"));

        var letType = new QuantifiedType(
                Lists.immutable.of(tyVarA, tyVarB),
                Type.function(tyVarA, tyVarB, tyVarA),
                new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE, TypeKind.INSTANCE));

        var letMeta = Meta.of(letName, letType);

        /*- let const[A, B](a: A, b: B): A = (a, b) -> a  */
        var originalNode = letFnNode(
                Meta.of(letName),
                "const",
                Lists.immutable.of(
                        forAllVarNode(Meta.of(tyVarAName), "A"),
                        forAllVarNode(Meta.of(tyVarBName), "B")),
                Lists.immutable.of(
                        ExampleNodes.Param.namedNode("a", typeRefNode(Meta.of(tyVarAName), "A")),
                        ExampleNodes.Param.namedNode("b", typeRefNode(Meta.of(tyVarBName), "B"))),
                typeRefNode(Meta.of(tyVarAName), "A"),
                ExampleNodes.LocalVar.namedNode("a"));

        var expectedNode = letFnNode(
                letMeta,
                "const",
                Lists.immutable.of(
                        forAllVarNode(tyVarAMeta, "A"),
                        forAllVarNode(tyVarBMeta, "B")),
                Lists.immutable.of(
                        ExampleNodes.Param.typedNode("a", tyVarA, typeRefNode(tyVarAMeta, "A")),
                        ExampleNodes.Param.typedNode("b", tyVarB, typeRefNode(tyVarBMeta, "B"))),
                typeRefNode(tyVarAMeta, "A"),
                ExampleNodes.LocalVar.typedNode("a", tyVarA));

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    // Expressions

    @Test
    @DisplayName("Empty block typechecks as Unit")
    void typecheckEmptyBlock() {
        var originalNode = blockNode(
                ExampleNodes.namelessMeta(),
                Optional.empty());

        var expectedNode = blockNode(
                ExampleNodes.namelessMeta(Type.UNIT),
                Optional.empty());

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("Block typechecks as its result type")
    void typecheckBlock() {
        var originalNode = blockNode(
                ExampleNodes.namelessMeta(),
                Optional.of(ExampleNodes.Int.namedNode(1)));

        var expectedNode = blockNode(
                ExampleNodes.namelessMeta(Type.INT),
                Optional.of(ExampleNodes.Int.typedNode(1)));

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("If expressions with boolean-typed condition and same consequent and alternative types typecheck successfully")
    void typecheckIf() {
        /* if true then 1 else 2 */
        var originalNode = ifNode(
                ExampleNodes.namelessMeta(),
                ExampleNodes.Boolean.namedNode(true),
                ExampleNodes.Int.namedNode(1),
                ExampleNodes.Int.namedNode(2));

        var expectedNode = ifNode(
                ExampleNodes.namelessMeta(Type.INT),
                ExampleNodes.Boolean.typedNode(true),
                ExampleNodes.Int.typedNode(1),
                ExampleNodes.Int.typedNode(2));

        testSuccessfulTypecheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("If expressions with ill-typed condition fail to typecheck")
    void typecheckIfIllTypedCondition() {
        var condRange = new Range(0, 1, 0, 1);

        /* if "true" then 1 else 2 */
        var originalNode = ifNode(
                ExampleNodes.namelessMeta(),
                stringNode(new Meta<>(condRange, Nameless.INSTANCE), "true"),
                ExampleNodes.Int.namedNode(1),
                ExampleNodes.Int.namedNode(2));

        var collector = testFailedTypecheck(TypeEnvironment.empty(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                condRange,
                "Mismatched type! Expected: Boolean, Actual: String");
    }

    @Test
    @DisplayName("If expressions with inconsistent consequent and alternative types fail to typecheck")
    void typecheckIfMismatchedBranchTypes() {
        var elseRange = new Range(0, 1, 0, 1);

        /* if true then 1 else "a" */
        var originalNode = ifNode(
                ExampleNodes.namelessMeta(),
                ExampleNodes.Boolean.namedNode(true),
                ExampleNodes.Int.namedNode(1),
                stringNode(new Meta<>(elseRange, Nameless.INSTANCE), "a"));

        var collector = testFailedTypecheck(TypeEnvironment.empty(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                elseRange,
                "Mismatched type! Expected: Int, Actual: String");
    }

    @Test
    @DisplayName("Nullary lambda typechecks successfully")
    void typecheckNullaryLambda() {
        /*- () -> 1 */
        var originalNode = lambdaNode(
                ExampleNodes.namelessMeta(),
                Lists.immutable.empty(),
                ExampleNodes.Int.namedNode(1));

        var expectedNode = lambdaNode(
                ExampleNodes.namelessMeta(Type.function(Type.INT)),
                Lists.immutable.empty(),
                ExampleNodes.Int.typedNode(1));

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("Annoted unary lambda typechecks successfully")
    void typecheckUnaryLambda() {
        /*- (x: Int) -> 1 */
        var originalNode = lambdaNode(
                ExampleNodes.namelessMeta(),
                Lists.immutable.of(
                        ExampleNodes.Param.namedNode("x", ExampleNodes.Int.NAMED_TYPE_NODE)),
                ExampleNodes.Int.namedNode(1));

        var expectedNode = lambdaNode(
                ExampleNodes.namelessMeta(Type.function(Type.INT, Type.INT)),
                Lists.immutable.of(
                        ExampleNodes.Param.typedNode("x", Type.INT,
                                ExampleNodes.Int.KINDED_TYPE_NODE)),
                ExampleNodes.Int.typedNode(1));

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("Unannotated unary lambda typechecks successfully when param type can be inferred")
    void typecheckUnaryLambdaIfExpression() {
        /*- bool -> if bool then 1 else 2 */
        var originalNode = lambdaNode(
                ExampleNodes.namelessMeta(),
                Lists.immutable.of(ExampleNodes.Param.namedNode("bool")),
                ifNode(
                        ExampleNodes.namelessMeta(),
                        ExampleNodes.LocalVar.namedNode("bool"),
                        ExampleNodes.Int.namedNode(1),
                        ExampleNodes.Int.namedNode(2)));

        var expectedNode = lambdaNode(
                ExampleNodes.namelessMeta(Type.function(Type.BOOLEAN, Type.INT)),
                Lists.immutable.of(ExampleNodes.Param.typedNode("bool", Type.BOOLEAN)),
                ifNode(
                        ExampleNodes.namelessMeta(Type.INT),
                        ExampleNodes.LocalVar.typedNode("bool", Type.BOOLEAN),
                        ExampleNodes.Int.typedNode(1),
                        ExampleNodes.Int.typedNode(2)));

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("Nullary application typechecks successfully")
    void typecheckNullaryApplication() {
        var environment = TypeEnvironment.withBuiltInTypes();

        /*- f: () -> Int */
        environment.putValue(
                "f",
                Meta.of(new LocalName("f", 0), Type.function(Type.INT)));

        /*- f() */
        var originalNode = applyNode(
                ExampleNodes.namelessMeta(),
                ExampleNodes.LocalVar.namedNode("f"),
                Lists.immutable.empty());

        var expectedNode = applyNode(
                ExampleNodes.namelessMeta(Type.INT),
                ExampleNodes.LocalVar.typedNode("f", Type.function(Type.INT)),
                Lists.immutable.empty());

        testSuccessfulTypecheck(environment, originalNode, expectedNode);
    }

    @Test
    @DisplayName("Unary application typechecks successfully")
    void typecheckUnaryApplication() {
        var environment = TypeEnvironment.withBuiltInTypes();

        /*- f: Int -> Int */
        environment.putValue(
                "f",
                Meta.of(new LocalName("f", 0), Type.function(Type.INT, Type.INT)));

        /*- f(1) */
        var originalNode = applyNode(
                ExampleNodes.namelessMeta(),
                ExampleNodes.LocalVar.namedNode("f"),
                Lists.immutable.of(ExampleNodes.Int.namedNode(1)));

        var expectedNode = applyNode(
                ExampleNodes.namelessMeta(Type.INT),
                ExampleNodes.LocalVar.typedNode("f", Type.function(Type.INT, Type.INT)),
                Lists.immutable.of(ExampleNodes.Int.typedNode(1)));

        testSuccessfulTypecheck(environment, originalNode, expectedNode);
    }

    @Test
    @DisplayName("Application of polymorphic identity function typechecks successfully")
    void typecheckPolyIdFnApplication() {
        var environment = TypeEnvironment.withBuiltInTypes();

        var tyVarA = new ForAllVar("A", TypeKind.INSTANCE);

        var idName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "id"));

        var idType = new QuantifiedType(
                Lists.immutable.of(tyVarA),
                Type.function(tyVarA, tyVarA),
                new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE));

        var idMeta = Meta.of(idName, idType);

        /*- id: [A] { A -> A }  */
        environment.putValue("id", idMeta);

        /*- id(1) */
        var originalNode = applyNode(
                ExampleNodes.namelessMeta(),
                refNode(Meta.of(idName), "id"),
                Lists.immutable.of(ExampleNodes.Int.namedNode(1)));

        var expectedNode = applyNode(
                ExampleNodes.namelessMeta(Type.INT),
                refNode(idMeta, "id"),
                Lists.immutable.of(ExampleNodes.Int.typedNode(1)));

        testSuccessfulTypecheck(environment, originalNode, expectedNode);
    }

    @Test
    @DisplayName("Application of polymorphic const function typechecks successfully at different instantiations")
    void typecheckPolyConstFnApplication() {
        var environment = TypeEnvironment.withBuiltInTypes();

        var tyVarA = new ForAllVar("A", TypeKind.INSTANCE);
        var tyVarB = new ForAllVar("B", TypeKind.INSTANCE);

        var constName = new LetName(new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, "const"));

        var constType = new QuantifiedType(
                Lists.immutable.of(tyVarA, tyVarB),
                Type.function(tyVarA, tyVarB, tyVarA),
                new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE, TypeKind.INSTANCE));

        var constMeta = Meta.of(constName, constType);

        /*- const: [A, B] { (A, B) -> A } */
        environment.putValue("const", constMeta);

        /*- const(const(1, 'a'), "b") */
        var originalNode = applyNode(
                ExampleNodes.namelessMeta(),
                refNode(Meta.of(constName), "const"),
                Lists.immutable.of(
                        applyNode(
                                ExampleNodes.namelessMeta(),
                                refNode(Meta.of(constName), "const"),
                                Lists.immutable.of(ExampleNodes.Int.namedNode(1), ExampleNodes.Char.namedNode('a'))),
                        ExampleNodes.String.namedNode("b")));

        var expectedNode = applyNode(
                ExampleNodes.namelessMeta(Type.INT),
                refNode(constMeta, "const"),
                Lists.immutable.of(
                        applyNode(
                                ExampleNodes.namelessMeta(Type.INT),
                                refNode(constMeta, "const"),
                                Lists.immutable.of(ExampleNodes.Int.typedNode(1), ExampleNodes.Char.typedNode('a'))),
                        ExampleNodes.String.typedNode("b")));

        testSuccessfulTypecheck(environment, originalNode, expectedNode);
    }

    @Test
    @DisplayName("Boolean not operator typechecks successfully")
    void typecheckBooleanNot() {
        var environment = TypeEnvironment.withBuiltInTypes();
        var originalNotNode = unaryOpNode(ExampleNodes.namelessMeta(), UnaryOp.BOOLEAN_NOT, boolNode(ExampleNodes.namelessMeta(), true));
        var expectedNotNode = unaryOpNode(Meta.nameless(Type.BOOLEAN), UnaryOp.BOOLEAN_NOT, boolNode(Meta.nameless(Type.BOOLEAN), true));
        testSuccessfulTypecheck(environment, originalNotNode, expectedNotNode);
    }

    @Test
    @DisplayName("Boolean not operator with invalid operand fails to typecheck")
    void typecheckBooleanNotInvalidOperand() {
        var environment = TypeEnvironment.withBuiltInTypes();

        var operandMeta = new Meta<Name>(new Range(0, 1, 0, 2), Nameless.INSTANCE);

        var originalNode = unaryOpNode(ExampleNodes.namelessMeta(), UnaryOp.BOOLEAN_NOT, intNode(operandMeta, 1));

        var collector = testFailedTypecheck(environment, originalNode);

        assertDiagnostic(
            collector.getDiagnostics(),
            operandMeta.range(),
            "Mismatched operand type! Expected: Boolean, Actual: Int");
    }

    @Test
    @DisplayName("Bitwise not operator typechecks successfully")
    void typecheckBitwiseNot() {
        var environment = TypeEnvironment.withBuiltInTypes();

        var intNotNode = unaryOpNode(ExampleNodes.namelessMeta(), UnaryOp.BITWISE_NOT, intNode(ExampleNodes.namelessMeta(), 1));
        var expectedIntNotNode = unaryOpNode(Meta.nameless(Type.INT), UnaryOp.BITWISE_NOT, intNode(Meta.nameless(Type.INT), 1));
        testSuccessfulTypecheck(environment, intNotNode, expectedIntNotNode);

        var longNotNode = unaryOpNode(ExampleNodes.namelessMeta(), UnaryOp.BITWISE_NOT, longNode(ExampleNodes.namelessMeta(), 1L));
        var expectedLongNotNode = unaryOpNode(Meta.nameless(Type.LONG), UnaryOp.BITWISE_NOT, longNode(Meta.nameless(Type.LONG), 1L));
        testSuccessfulTypecheck(environment, longNotNode, expectedLongNotNode);
    }

    @Test
    @DisplayName("Bitwise not operator with invalid operand fails to typecheck")
    void typecheckBitwiseNotInvalidOperand() {
        var environment = TypeEnvironment.withBuiltInTypes();

        var operandMeta = new Meta<Name>(new Range(0, 1, 0, 2), Nameless.INSTANCE);

        var originalNode = unaryOpNode(ExampleNodes.namelessMeta(), UnaryOp.BITWISE_NOT, boolNode(operandMeta, true));

        var collector = testFailedTypecheck(environment, originalNode);

        assertDiagnostic(
            collector.getDiagnostics(),
            operandMeta.range(),
            "Mismatched operand type! Expected: An integral type, Actual: Boolean");
    }

    @Test
    @DisplayName("Negate operator typechecks successfully")
    void typecheckNegate() {
        var environment = TypeEnvironment.withBuiltInTypes();

        var intNotNode = unaryOpNode(ExampleNodes.namelessMeta(), UnaryOp.NEGATE, intNode(ExampleNodes.namelessMeta(), 1));
        var expectedIntNotNode = unaryOpNode(Meta.nameless(Type.INT), UnaryOp.NEGATE, intNode(Meta.nameless(Type.INT), 1));
        testSuccessfulTypecheck(environment, intNotNode, expectedIntNotNode);

        var longNotNode = unaryOpNode(ExampleNodes.namelessMeta(), UnaryOp.NEGATE, longNode(ExampleNodes.namelessMeta(), 1L));
        var expectedLongNotNode = unaryOpNode(Meta.nameless(Type.LONG), UnaryOp.NEGATE, longNode(Meta.nameless(Type.LONG), 1L));
        testSuccessfulTypecheck(environment, longNotNode, expectedLongNotNode);
    }

    @Test
    @DisplayName("Negate operator with invalid operand fails to typecheck")
    void typecheckNegateInvalidOperand() {
        var environment = TypeEnvironment.withBuiltInTypes();

        var operandMeta = new Meta<Name>(new Range(0, 1, 0, 2), Nameless.INSTANCE);

        var originalNode = unaryOpNode(ExampleNodes.namelessMeta(), UnaryOp.NEGATE, boolNode(operandMeta, true));

        var collector = testFailedTypecheck(environment, originalNode);

        assertDiagnostic(
            collector.getDiagnostics(),
            operandMeta.range(),
            "Mismatched operand type! Expected: A numeric type, Actual: Boolean");
    }

    @ParameterizedTest(name = "Numeric binary operators typecheck successfully - {0}")
    @EnumSource(
        value = BinaryOp.class,
        names = {
            "POWER",
            "MULTIPLY",
            "DIVIDE",
            "MODULUS",
            "ADD",
            "SUBTRACT",
            "LESS_THAN",
            "LESS_THAN_EQUAL",
            "GREATER_THAN",
            "GREATER_THAN_EQUAL"
    })
    void typecheckNumericBinary(BinaryOp binaryOp) {
        var environment = TypeEnvironment.withBuiltInTypes();

        var intBinaryNode = binaryOpNode(
            ExampleNodes.namelessMeta(),
            intNode(ExampleNodes.namelessMeta(), 1),
            binaryOp,
            intNode(ExampleNodes.namelessMeta(), 2));

        var expectedIntBinaryNode = binaryOpNode(
            Meta.nameless(Type.INT),
            intNode(Meta.nameless(Type.INT), 1),
            binaryOp,
            intNode(Meta.nameless(Type.INT), 2));

        testSuccessfulTypecheck(environment, intBinaryNode, expectedIntBinaryNode);

        var longBinaryNode = binaryOpNode(
            ExampleNodes.namelessMeta(),
            longNode(ExampleNodes.namelessMeta(), 1L),
            binaryOp,
            longNode(ExampleNodes.namelessMeta(), 2L));

        var expectedLongBinaryNode = binaryOpNode(
            Meta.nameless(Type.LONG),
            longNode(Meta.nameless(Type.LONG), 1L),
            binaryOp,
            longNode(Meta.nameless(Type.LONG), 2L));

        testSuccessfulTypecheck(environment, longBinaryNode, expectedLongBinaryNode);

        var floatBinaryNode = binaryOpNode(
            ExampleNodes.namelessMeta(),
            floatNode(ExampleNodes.namelessMeta(), 1.0F),
            binaryOp,
            floatNode(ExampleNodes.namelessMeta(), 2.0F));

        var expectedFloatBinaryNode = binaryOpNode(
            Meta.nameless(Type.FLOAT),
            floatNode(Meta.nameless(Type.FLOAT), 1.0F),
            binaryOp,
            floatNode(Meta.nameless(Type.FLOAT), 2.0F));

        testSuccessfulTypecheck(environment, floatBinaryNode, expectedFloatBinaryNode);

        var doubleBinaryNode = binaryOpNode(
            ExampleNodes.namelessMeta(),
            doubleNode(ExampleNodes.namelessMeta(), 1.0),
            binaryOp,
            doubleNode(ExampleNodes.namelessMeta(), 2.0));

        var expectedDoubleBinaryNode = binaryOpNode(
            Meta.nameless(Type.DOUBLE),
            doubleNode(Meta.nameless(Type.DOUBLE), 1.0),
            binaryOp,
            doubleNode(Meta.nameless(Type.DOUBLE), 2.0));

        testSuccessfulTypecheck(environment, doubleBinaryNode, expectedDoubleBinaryNode);
    }

    @ParameterizedTest(name = "Numeric binary operators with mismatched operands fail to typecheck - {0}")
    @EnumSource(
        value = BinaryOp.class,
        names = {
            "POWER",
            "MULTIPLY",
            "DIVIDE",
            "MODULUS",
            "ADD",
            "SUBTRACT",
            "LESS_THAN",
            "LESS_THAN_EQUAL",
            "GREATER_THAN",
            "GREATER_THAN_EQUAL"
    })
    void typecheckNumericBinaryMismatchedOperands(BinaryOp binaryOp) {
        var environment = TypeEnvironment.withBuiltInTypes();

        var longOperandMeta = new Meta<Name>(new Range(0, 1, 0, 1), Nameless.INSTANCE);

        var mixedIntLongNode = binaryOpNode(
            ExampleNodes.namelessMeta(),
            intNode(ExampleNodes.namelessMeta(), 1),
            binaryOp,
            longNode(longOperandMeta, 2L));

        var intLongCollector = testFailedTypecheck(environment, mixedIntLongNode);

        assertDiagnostic(
            intLongCollector.getDiagnostics(),
            longOperandMeta.range(),
            "Mismatched type! Expected: Int, Actual: Long");

        var floatOperandMeta = new Meta<Name>(new Range(0, 1, 0, 2), Nameless.INSTANCE);

        var mixedIntFloatNode = binaryOpNode(
            ExampleNodes.namelessMeta(),
            intNode(ExampleNodes.namelessMeta(), 1),
            binaryOp,
            floatNode(floatOperandMeta, 2.0F));

        var intFloatCollector = testFailedTypecheck(environment, mixedIntFloatNode);

        assertDiagnostic(
            intFloatCollector.getDiagnostics(),
            floatOperandMeta.range(),
            "Mismatched type! Expected: Int, Actual: Float");
    }

    @ParameterizedTest(name = "Integral binary operators typecheck successfully - {0}")
    @EnumSource(
        value = BinaryOp.class,
        names = {
        "SHIFT_LEFT",
        "SHIFT_RIGHT",
        "UNSIGNED_SHIFT_RIGHT",
        "BITWISE_AND",
        "BITWISE_OR",
        "BITWISE_XOR"
    })
    void typecheckIntegralBinary(BinaryOp binaryOp) {
        var environment = TypeEnvironment.withBuiltInTypes();

        var intBinaryNode = binaryOpNode(
            ExampleNodes.namelessMeta(),
            intNode(ExampleNodes.namelessMeta(), 1),
            binaryOp,
            intNode(ExampleNodes.namelessMeta(), 2));

        var expectedIntBinaryNode = binaryOpNode(
            Meta.nameless(Type.INT),
            intNode(Meta.nameless(Type.INT), 1),
            binaryOp,
            intNode(Meta.nameless(Type.INT), 2));

        testSuccessfulTypecheck(environment, intBinaryNode, expectedIntBinaryNode);

        var longBinaryNode = binaryOpNode(
            ExampleNodes.namelessMeta(),
            longNode(ExampleNodes.namelessMeta(), 1L),
            binaryOp,
            longNode(ExampleNodes.namelessMeta(), 2L));

        var expectedLongBinaryNode = binaryOpNode(
            Meta.nameless(Type.LONG),
            longNode(Meta.nameless(Type.LONG), 1L),
            binaryOp,
            longNode(Meta.nameless(Type.LONG), 2L));

        testSuccessfulTypecheck(environment, longBinaryNode, expectedLongBinaryNode);
    }

    @ParameterizedTest(name = "Integral binary operators with mismatched operands fail to typecheck - {0}")
    @EnumSource(
        value = BinaryOp.class,
        names = {
            "SHIFT_LEFT",
            "SHIFT_RIGHT",
            "UNSIGNED_SHIFT_RIGHT",
            "BITWISE_AND",
            "BITWISE_OR",
            "BITWISE_XOR"}
    )
    void typecheckIntegralBinaryMismatchedOperands(BinaryOp binaryOp) {
        var environment = TypeEnvironment.withBuiltInTypes();

        var longOperandMeta = new Meta<Name>(new Range(0, 1, 0, 1), Nameless.INSTANCE);

        var mixedIntLongNode = binaryOpNode(
            ExampleNodes.namelessMeta(),
            intNode(ExampleNodes.namelessMeta(), 1),
            binaryOp,
            longNode(longOperandMeta, 2L));

        var collector = testFailedTypecheck(environment, mixedIntLongNode);

        assertDiagnostic(
            collector.getDiagnostics(),
            longOperandMeta.range(),
            "Mismatched type! Expected: Int, Actual: Long");
    }

    @ParameterizedTest(name = "Boolean binary operators typecheck successfully - {0}")
    @EnumSource(
        value = BinaryOp.class,
        names = {
        "BITWISE_AND",
        "BITWISE_OR",
        "BITWISE_XOR",
        "BOOLEAN_AND",
        "BOOLEAN_OR"
    })
    void typecheckBooleanBinary(BinaryOp binaryOp) {
        var environment = TypeEnvironment.withBuiltInTypes();

        var boolBinaryNode = binaryOpNode(
            ExampleNodes.namelessMeta(),
            boolNode(ExampleNodes.namelessMeta(), true),
            binaryOp,
            boolNode(ExampleNodes.namelessMeta(), false));

        var expectedBoolBinaryNode = binaryOpNode(
            Meta.nameless(Type.BOOLEAN),
            boolNode(Meta.nameless(Type.BOOLEAN), true),
            binaryOp,
            boolNode(Meta.nameless(Type.BOOLEAN), false));

        testSuccessfulTypecheck(environment, boolBinaryNode, expectedBoolBinaryNode);
    }

    @Test
    @DisplayName("References to variables in the environment typecheck successfully")
    void typecheckReference() {
        var environment = TypeEnvironment.withBuiltInTypes();
        environment.putType("Bool", ExampleNodes.Bool.KINDED_META);
        environment.putValue("True", ExampleNodes.True.TYPED_META);
        environment.putValue("False", ExampleNodes.False.TYPED_META);

        var originalTrueNode = refNode(Meta.<Name>of(ExampleNodes.True.NAME), "True");
        var expectedTrueNode = refNode(ExampleNodes.True.TYPED_META, "True");
        testSuccessfulTypecheck(environment, originalTrueNode, expectedTrueNode);

        var originalFalseNode = refNode(Meta.<Name>of(ExampleNodes.False.NAME), "False");
        var expectedFalseNode = refNode(ExampleNodes.False.TYPED_META, "False");
        testSuccessfulTypecheck(environment, originalFalseNode, expectedFalseNode);
    }

    // Literals

    @Test
    @DisplayName("Boolean literals typecheck successfully")
    void typecheckLiteralBoolean() {
        /* true */
        var originalNode = ExampleNodes.Boolean.namedNode(true);
        var expectedNode = ExampleNodes.Boolean.typedNode(true);
        testSuccessfulTypecheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("Char literals typecheck successfully")
    void typecheckLiteralChar() {
        /* 'c' */
        var originalNode = ExampleNodes.Char.namedNode('c');
        var expectedNode = ExampleNodes.Char.typedNode('c');
        testSuccessfulTypecheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("String literals typecheck successfully")
    void typecheckLiteralString() {
        /* "foo" */
        var originalNode = ExampleNodes.String.namedNode("foo");
        var expectedNode = ExampleNodes.String.typedNode("foo");
        testSuccessfulTypecheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("Int literals typecheck successfully")
    void typecheckLiteralInt() {
        /* 1 */
        var originalNode = ExampleNodes.Int.namedNode(1);
        var expectedNode = ExampleNodes.Int.typedNode(1);
        testSuccessfulTypecheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("Long literals typecheck successfully")
    void typecheckLiteralLong() {
        /* 1L */
        var originalNode = ExampleNodes.Long.namedNode(1L);
        var expectedNode = ExampleNodes.Long.typedNode(1L);
        testSuccessfulTypecheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("Float literals typecheck successfully")
    void typecheckLiteralFloat() {
        /* 0.1F */
        var originalNode = ExampleNodes.Float.namedNode(0.1F);
        var expectedNode = ExampleNodes.Float.typedNode(0.1F);
        testSuccessfulTypecheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    @DisplayName("Double literals typecheck successfully")
    void typecheckLiteralDouble() {
        /* 0.1 */
        var originalNode = ExampleNodes.Double.namedNode(0.1);
        var expectedNode = ExampleNodes.Double.typedNode(0.1);
        testSuccessfulTypecheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    // Subtype checking

    @Test
    @DisplayName("Identity function type is subtype of its instantiations")
    void checkIdPolyInstantiationSubtyping() {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Typechecker.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var typechecker = new Typechecker(scopedCollector, TypeEnvironment.withBuiltInTypes());

        var tyVarA = new ForAllVar("A", TypeKind.INSTANCE);

        var idPoly = new QuantifiedType(
                Lists.immutable.of(tyVarA),
                Type.function(tyVarA, tyVarA),
                new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE));

        var idInstantiation = Type.function(Type.INT, Type.INT);

        assertThat(typechecker.checkSubType(idPoly, idInstantiation), is(true));
    }

    @Test
    @DisplayName("Identity function type is not supertype of its instantiations")
    void checkIdPolyInstantiationSupertyping() {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Typechecker.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var typechecker = new Typechecker(scopedCollector, TypeEnvironment.withBuiltInTypes());

        var tyVarA = new ForAllVar("A", TypeKind.INSTANCE);

        var idPoly = new QuantifiedType(
                Lists.immutable.of(tyVarA),
                Type.function(tyVarA, tyVarA),
                new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE));

        var idInstantiation = Type.function(Type.INT, Type.INT);

        assertThat(typechecker.checkSubType(idInstantiation, idPoly), is(false));
    }

    @Property
    @Label("Unsolved types are supertype of any other type")
    void checkUnsolvedSuperTyping(@ForAll("types") Type type) {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Typechecker.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var typechecker = new Typechecker(scopedCollector, TypeEnvironment.withBuiltInTypes());
        var unsolved = typechecker.newUnsolvedType(TypeKind.INSTANCE);
        assertThat(typechecker.checkSubType(type, unsolved), is(true));
    }

    @Property
    @Label("Unsolved types are subtype of any other type")
    void checkUnsolvedSubTyping(@ForAll("types") Type type) {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Typechecker.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var typechecker = new Typechecker(scopedCollector, TypeEnvironment.withBuiltInTypes());
        var unsolved = typechecker.newUnsolvedType(TypeKind.INSTANCE);
        assertThat(typechecker.checkSubType(unsolved, type), is(true));
    }

    @Property
    @Label("Subtype reflexivity - unsolved types are subtype of themselves")
    void checkUnsolvedSubTypeReflexivity() {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Typechecker.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var typechecker = new Typechecker(scopedCollector, TypeEnvironment.withBuiltInTypes());
        var unsolved = typechecker.newUnsolvedType(TypeKind.INSTANCE);
        assertThat(typechecker.checkSubType(unsolved, unsolved), is(true));
    }

    @Property
    @Label("Subtype reflexivity - types are subtype of themselves")
    void checkSubTypeReflexivity(@ForAll("types") Type type) {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Typechecker.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var typechecker = new Typechecker(scopedCollector, TypeEnvironment.withBuiltInTypes());
        assertThat(typechecker.checkSubType(type, type), is(true));
    }

    @Provide
    Arbitrary<Type> types() {
        return Arbitraries.lazyOf(
                () -> simpleTypes(),
                () -> simpleTypes().list().ofMinSize(1).ofMaxSize(4).map(types -> {
                    var tyList = Lists.immutable.ofAll(types);
                    var funArgTys = tyList.take(tyList.size() - 1);
                    var funReturnTy = tyList.getLast();
                    return Type.function(funArgTys, funReturnTy);
                }),
                () -> simpleTypes().list().ofMinSize(1).ofMaxSize(3).flatMap(types -> {
                    var tyArgs = Lists.immutable.ofAll(types);
                    var tyArgKinds = Lists.mutable.withNValues(
                            types.size() + 1,
                            () -> (Kind) TypeKind.INSTANCE);
                    var tyConKind = new HigherKind(tyArgKinds.toImmutable());
                    return Arbitraries.strings()
                            .ofMinLength(2)
                            .ofMaxLength(20)
                            .map(tyName -> {
                                var tyConName = new QualifiedName(
                                        new NamespaceName(Lists.immutable
                                                .of("Mina", "Test"),
                                                "Typechecker"),
                                        tyName);
                                return new TypeApply(
                                        new TypeConstructor(tyConName,
                                                tyConKind),
                                        tyArgs,
                                        TypeKind.INSTANCE);
                            });
                }));
    }

    @Provide
    Arbitrary<Type> simpleTypes() {
        return Arbitraries.oneOf(tyVars(), tyCons(), builtIns());
    }

    @Provide
    Arbitrary<TypeVar> tyVars() {
        return Arbitraries.oneOf(
                Arbitraries.chars()
                        .filter(Character::isAlphabetic)
                        .map(c -> new ForAllVar(c.toString(), TypeKind.INSTANCE)),
                Arbitraries.chars()
                        .filter(Character::isAlphabetic)
                        .map(c -> new ExistsVar("?" + c.toString(), TypeKind.INSTANCE)));
    }

    @Provide
    Arbitrary<TypeConstructor> tyCons() {
        return Arbitraries.strings()
                .ofMaxLength(20)
                .map(name -> new TypeConstructor(
                        new QualifiedName(ExampleNodes.TYPECHECKER_NAMESPACE, name),
                        TypeKind.INSTANCE));
    }

    @Provide
    Arbitrary<BuiltInType> builtIns() {
        return Arbitraries.of(Type.builtIns.toSet());
    }
}

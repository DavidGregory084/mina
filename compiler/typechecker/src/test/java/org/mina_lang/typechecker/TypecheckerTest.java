package org.mina_lang.typechecker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mina_lang.syntax.SyntaxNodes.*;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Range;
import org.mina_lang.common.TypeEnvironment;
import org.mina_lang.common.diagnostics.Diagnostic;
import org.mina_lang.common.names.*;
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.*;

import net.jqwik.api.*;
import net.jqwik.api.Tuple.Tuple2;
import net.jqwik.api.Tuple.Tuple3;

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
            DeclarationNode<Name> originalNode) {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, environment);
        typechecker.typecheck(originalNode);
        var errors = diagnostics.getErrors();
        assertThat("There should be type errors", errors, is(not(empty())));
        return diagnostics;
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
                Meta.of(new Attributes(letName, expectedType)),
                "testLiteral",
                typeRefNode(
                        Meta.of(new Attributes(builtInName, TypeKind.INSTANCE)),
                        expectedType.name()),
                expectedLiteralNode);

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
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
                Meta.of(new Attributes(letName, expectedType)),
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
                Meta.of(new Attributes(letName, ExampleNodes.True.TYPE)),
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
                Meta.of(new Attributes(letName, ExampleNodes.True.TYPE)),
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
                Meta.of(new Attributes(letName, Type.function(Type.INT, Type.INT))),
                "testAnnotatedLambda",
                funTypeNode(
                        Meta.of(new Attributes(Nameless.INSTANCE, TypeKind.INSTANCE)),
                        Lists.immutable.of(typeRefNode(Meta.of(new Attributes(
                                new BuiltInName("Int"), TypeKind.INSTANCE)), "Int")),
                        typeRefNode(Meta.of(new Attributes(new BuiltInName("Int"),
                                TypeKind.INSTANCE)), "Int")),
                lambdaNode(
                        Meta.of(new Attributes(Nameless.INSTANCE,
                                Type.function(Type.INT, Type.INT))),
                        Lists.immutable.of(paramNode(Meta.of(
                                new Attributes(new LocalName("i", 0), Type.INT)), "i")),
                        refNode(Meta.of(new Attributes(new LocalName("i", 0), Type.INT)),
                                "i")));

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
                                        Lists.immutable.of(ExampleNodes.Int.NAMED_TYPE_NODE),
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
                Meta.of(new Attributes(letName, intToIntToInt)),
                "testAnnotatedLambda",
                funTypeNode(
                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                        Lists.immutable.of(
                                funTypeNode(
                                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                                        Lists.immutable.of(ExampleNodes.Int.KINDED_TYPE_NODE),
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
    @DisplayName("Annotated let function with function argument typechecks successfully without parameter annotations")
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
                Meta.of(new Attributes(letName, intToIntToInt)),
                "testAnnotatedLambda",
                Lists.immutable.of(ExampleNodes.Param.typedNode("f", intToInt)),
                ExampleNodes.Int.KINDED_TYPE_NODE,
                applyNode(
                        ExampleNodes.namelessMeta(Type.INT),
                        ExampleNodes.LocalVar.typedNode("f", intToInt),
                        Lists.immutable.of(ExampleNodes.Int.typedNode(1))));

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    // Expressions

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
                Meta.of(new Attributes(new LocalName("f", 0), Type.function(Type.INT))));

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
                Meta.of(new Attributes(new LocalName("f", 0), Type.function(Type.INT, Type.INT))));

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

    @Property
    @Label("Unsolved types are supertype of any other type")
    void checkUnsolvedSuperTyping(@ForAll("types") Type type) {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, TypeEnvironment.withBuiltInTypes());
        var unsolved = typechecker.newUnsolvedType(TypeKind.INSTANCE);
        assertThat(typechecker.checkSubType(type, unsolved), is(true));
    }

    @Property
    @Label("Unsolved types are subtype of any other type")
    void checkUnsolvedSubTyping(@ForAll("types") Type type) {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, TypeEnvironment.withBuiltInTypes());
        var unsolved = typechecker.newUnsolvedType(TypeKind.INSTANCE);
        assertThat(typechecker.checkSubType(unsolved, type), is(true));
    }

    @Property
    @Label("Subtype reflexivity - unsolved types are subtype of themselves")
    void checkUnsolvedSubTypeReflexivity() {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, TypeEnvironment.withBuiltInTypes());
        var unsolved = typechecker.newUnsolvedType(TypeKind.INSTANCE);
        assertThat(typechecker.checkSubType(unsolved, unsolved), is(true));
    }

    @Property
    @Label("Subtype reflexivity - types are subtype of themselves")
    void checkSubTypeReflexivity(@ForAll("types") Type type) {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, TypeEnvironment.withBuiltInTypes());
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
                        new QualifiedName(new NamespaceName(Lists.immutable.of("Mina", "Test"),
                                "Typechecker"), name),
                        TypeKind.INSTANCE));
    }

    @Provide
    Arbitrary<BuiltInType> builtIns() {
        return Arbitraries.of(Type.builtIns.toSet());
    }
}

package org.mina_lang.typechecker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mina_lang.syntax.SyntaxNodes.*;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;
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
                        .map(b -> Tuple.of(boolNode(Meta.of(Nameless.INSTANCE), b), Type.BOOLEAN)),
                Arbitraries.chars()
                        .map(c -> Tuple.of(charNode(Meta.of(Nameless.INSTANCE), c), Type.CHAR)),
                Arbitraries.strings().ofMaxLength(100)
                        .map(s -> Tuple.of(stringNode(Meta.of(Nameless.INSTANCE), s), Type.STRING)),
                Arbitraries.integers()
                        .map(i -> Tuple.of(intNode(Meta.of(Nameless.INSTANCE), i), Type.INT)),
                Arbitraries.longs()
                        .map(l -> Tuple.of(longNode(Meta.of(Nameless.INSTANCE), l), Type.LONG)),
                Arbitraries.floats()
                        .map(f -> Tuple.of(floatNode(Meta.of(Nameless.INSTANCE), f), Type.FLOAT)),
                Arbitraries.doubles()
                        .map(d -> Tuple.of(doubleNode(Meta.of(Nameless.INSTANCE), d), Type.DOUBLE)));
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
    void typecheckAnnotatedLiteralLetBinding(@ForAll("literals") Tuple2<LiteralNode<Name>, BuiltInType> tuple) {
        var originalLiteralNode = tuple.get1();
        var expectedType = tuple.get2();

        var expectedLiteralNode = originalLiteralNode.accept(new LiteralNodeMetaTransformer<Name, Attributes>() {
            @Override
            public Meta<Attributes> updateMeta(Meta<Name> meta) {
                return meta.withMeta(new Attributes(meta.meta(), expectedType));
            }
        });

        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Typechecker");
        var qualName = new QualifiedName(nsName, "testLiteral");
        var letName = new LetName(qualName);

        var builtInName = new BuiltInName(expectedType.name());

        var originalNode = letNode(
                Meta.of(letName),
                "testLiteral",
                typeRefNode(Meta.of(builtInName), expectedType.name()),
                originalLiteralNode);

        var expectedNode = letNode(
                Meta.of(new Attributes(letName, expectedType)),
                "testLiteral",
                typeRefNode(Meta.of(new Attributes(builtInName, TypeKind.INSTANCE)), expectedType.name()),
                expectedLiteralNode);

        testSuccessfulTypecheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Property
    void typecheckUnannotatedLiteralLetBinding(@ForAll("literals") Tuple2<LiteralNode<Name>, BuiltInType> tuple) {
        var originalLiteralNode = tuple.get1();
        var expectedType = tuple.get2();

        var expectedLiteralNode = originalLiteralNode.accept(new LiteralNodeMetaTransformer<Name, Attributes>() {
            @Override
            public Meta<Attributes> updateMeta(Meta<Name> meta) {
                return meta.withMeta(new Attributes(meta.meta(), expectedType));
            }
        });

        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Typechecker");
        var qualName = new QualifiedName(nsName, "testLiteral");
        var letName = new LetName(qualName);

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

    @Property
    void typecheckWrongLetAnnotation(@ForAll("illTypedLiterals") Tuple3<LiteralNode<Name>, BuiltInType, BuiltInType> tuple) {
        var originalLiteralNode = tuple.get1();
        var actualLiteralType = tuple.get2();
        var incorrectExpectedType = tuple.get3();

        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Typechecker");
        var qualName = new QualifiedName(nsName, "testLiteral");
        var letName = new LetName(qualName);

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
            "Mismatched type! Expected: " + incorrectExpectedType.name() + ", Actual: " + actualLiteralType.name());
    }

    @Test
    void typecheckAnnotatedLetBoundReference() {
        var environment = TypeEnvironment.withBuiltInTypes();

        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Typechecker");

        var boolName = new DataName(new QualifiedName(nsName, "Bool"));
        var boolMeta = Meta.of(new Attributes(boolName, TypeKind.INSTANCE));

        var boolConstrType = Type.function(
                        Lists.immutable.empty(),
                        new TypeConstructor(boolName.name(), TypeKind.INSTANCE));

        var trueName = new ConstructorName(boolName, new QualifiedName(nsName, "True"));

        var trueMeta = Meta.of(new Attributes(trueName, boolConstrType));

        var falseName = new ConstructorName(boolName, new QualifiedName(nsName, "False"));

        var falseMeta = Meta.of(new Attributes(falseName, boolConstrType));

        environment.putType("Bool", boolMeta);
        environment.putValue("True", trueMeta);
        environment.putValue("False", falseMeta);

        var originalBoolNode = refNode(trueMeta.withMeta(trueMeta.meta().name()), "True");
        var expectedBoolNode = refNode(trueMeta, "True");

        var qualLetName = new QualifiedName(nsName, "testBool");
        var letName = new LetName(qualLetName);

        var originalBoolConstrTypeNode = funTypeNode(
            Meta.of(Nameless.INSTANCE),
            Lists.immutable.empty(),
            typeRefNode(Meta.of(boolName), "Bool"));

        var expectedBoolConstrTypeNode = funTypeNode(
            Meta.of(new Attributes(Nameless.INSTANCE, TypeKind.INSTANCE)),
            Lists.immutable.empty(),
            typeRefNode(Meta.of(new Attributes(boolName, TypeKind.INSTANCE)), "Bool"));

        var originalNode = letNode(
                Meta.of(letName),
                "testBool",
                originalBoolConstrTypeNode,
                originalBoolNode);

        var expectedNode = letNode(
                Meta.of(new Attributes(letName, boolConstrType)),
                "testBool",
                expectedBoolConstrTypeNode,
                expectedBoolNode);

        testSuccessfulTypecheck(environment, originalNode, expectedNode);
    }

    @Test
    void typecheckIllTypedLetBoundReference() {
        var environment = TypeEnvironment.withBuiltInTypes();

        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Typechecker");

        var boolName = new DataName(new QualifiedName(nsName, "Bool"));
        var boolMeta = Meta.of(new Attributes(boolName, TypeKind.INSTANCE));

        var boolConstrType = Type.function(
                        Lists.immutable.empty(),
                        new TypeConstructor(boolName.name(), TypeKind.INSTANCE));

        var trueName = new ConstructorName(boolName, new QualifiedName(nsName, "True"));

        var trueMeta = Meta.of(new Attributes(trueName, boolConstrType));

        var falseName = new ConstructorName(boolName, new QualifiedName(nsName, "False"));

        var falseMeta = Meta.of(new Attributes(falseName, boolConstrType));

        environment.putType("Bool", boolMeta);
        environment.putValue("True", trueMeta);
        environment.putValue("False", falseMeta);

        var boolReferenceRange = new Range(0, 1, 0, 1);
        var originalBoolNode = refNode(new Meta<>(boolReferenceRange, trueMeta.meta().name()), "True");

        var qualLetName = new QualifiedName(nsName, "testBool");
        var letName = new LetName(qualLetName);

        var originalBoolConstrTypeNode = funTypeNode(
            Meta.of(Nameless.INSTANCE),
            Lists.immutable.of(typeRefNode(Meta.of(new BuiltInName("Int")), "Int")),
            typeRefNode(Meta.of(boolName), "Bool"));

        var originalNode = letNode(
                Meta.of(letName),
                "testBool",
                originalBoolConstrTypeNode,
                originalBoolNode);

        var collector = testFailedTypecheck(environment, originalNode);

        assertDiagnostic(
            collector.getDiagnostics(),
            boolReferenceRange,
            "Mismatched type! Expected: Int -> Bool, Actual: () -> Bool");
    }

    @Test
    void typecheckUnannotatedLetBoundReference() {
        var environment = TypeEnvironment.withBuiltInTypes();

        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Typechecker");

        var boolName = new DataName(new QualifiedName(nsName, "Bool"));
        var boolMeta = Meta.of(new Attributes(boolName, TypeKind.INSTANCE));

        var boolConstrType = Type.function(
                        Lists.immutable.empty(),
                        new TypeConstructor(boolName.name(), TypeKind.INSTANCE));

        var trueName = new ConstructorName(boolName, new QualifiedName(nsName, "True"));

        var trueMeta = Meta.of(new Attributes(trueName, boolConstrType));

        var falseName = new ConstructorName(boolName, new QualifiedName(nsName, "False"));

        var falseMeta = Meta.of(new Attributes(falseName, boolConstrType));

        environment.putType("Bool", boolMeta);
        environment.putValue("True", trueMeta);
        environment.putValue("False", falseMeta);

        var originalBoolNode = refNode(trueMeta.withMeta(trueMeta.meta().name()), "True");
        var expectedBoolNode = refNode(trueMeta, "True");

        var qualLetName = new QualifiedName(nsName, "testBool");
        var letName = new LetName(qualLetName);

        var originalNode = letNode(
                Meta.of(letName),
                "testBool",
                originalBoolNode);

        var expectedNode = letNode(
                Meta.of(new Attributes(letName, boolConstrType)),
                "testBool",
                expectedBoolNode);

        testSuccessfulTypecheck(environment, originalNode, expectedNode);
    }


    @Test
    void typecheckReference() {
        var environment = TypeEnvironment.withBuiltInTypes();

        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Typechecker");

        var boolName = new DataName(new QualifiedName(nsName, "Bool"));
        var boolMeta = Meta.of(new Attributes(boolName, TypeKind.INSTANCE));

        var trueName = new ConstructorName(boolName, new QualifiedName(nsName, "True"));

        var trueMeta = Meta.of(new Attributes(
                trueName,
                Type.function(
                        Lists.immutable.empty(),
                        new TypeConstructor(trueName.name(), TypeKind.INSTANCE))));

        var falseName = new ConstructorName(boolName, new QualifiedName(nsName, "False"));

        var falseMeta = Meta.of(new Attributes(
                falseName,
                Type.function(
                        Lists.immutable.empty(),
                        new TypeConstructor(falseName.name(), TypeKind.INSTANCE))));

        environment.putType("Bool", boolMeta);
        environment.putValue("True", trueMeta);
        environment.putValue("False", falseMeta);

        var originalTrueNode = refNode(trueMeta.withMeta(trueMeta.meta().name()), "True");
        var expectedTrueNode = refNode(trueMeta, "True");

        testSuccessfulTypecheck(environment, originalTrueNode, expectedTrueNode);

        var originalFalseNode = refNode(falseMeta.withMeta(falseMeta.meta().name()), "False");
        var expectedFalseNode = refNode(falseMeta, "False");

        testSuccessfulTypecheck(environment, originalFalseNode, expectedFalseNode);
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
    void checkUnsolvedSuperTyping(@ForAll("types") Type type) {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, TypeEnvironment.withBuiltInTypes());
        var unsolved = typechecker.newUnsolvedType(TypeKind.INSTANCE);
        assertThat(typechecker.checkSubType(type, unsolved), is(true));
    }

    @Property
    void checkUnsolvedSubTyping(@ForAll("types") Type type) {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, TypeEnvironment.withBuiltInTypes());
        var unsolved = typechecker.newUnsolvedType(TypeKind.INSTANCE);
        assertThat(typechecker.checkSubType(unsolved, type), is(true));
    }

    @Property
    void checkUnsolvedSubTypeReflexivity() {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, TypeEnvironment.withBuiltInTypes());
        var unsolved = typechecker.newUnsolvedType(TypeKind.INSTANCE);
        assertThat(typechecker.checkSubType(unsolved, unsolved), is(true));
    }

    @Property
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
                                        new NamespaceName(Lists.immutable.of("Mina", "Test"), "Typechecker"), tyName);
                                return new TypeApply(
                                        new TypeConstructor(tyConName, tyConKind),
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
                        new QualifiedName(new NamespaceName(Lists.immutable.of("Mina", "Test"), "Typechecker"), name),
                        TypeKind.INSTANCE));
    }

    @Provide
    Arbitrary<BuiltInType> builtIns() {
        return Arbitraries.of(Type.builtIns.toSet());
    }
}

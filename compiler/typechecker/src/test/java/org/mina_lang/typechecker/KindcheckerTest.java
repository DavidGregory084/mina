package org.mina_lang.typechecker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mina_lang.syntax.SyntaxNodes.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Test;
import org.mina_lang.common.*;
import org.mina_lang.common.diagnostics.Diagnostic;
import org.mina_lang.common.names.*;
import org.mina_lang.common.scopes.BuiltInScope;
import org.mina_lang.common.scopes.ImportedScope;
import org.mina_lang.common.types.HigherKind;
import org.mina_lang.common.types.TypeKind;
import org.mina_lang.common.types.UnsolvedVariableSupply;
import org.mina_lang.syntax.DataNode;
import org.mina_lang.syntax.TypeNode;

import net.jqwik.api.*;

public class KindcheckerTest {
    void testSuccessfulKindcheck(
            TypeEnvironment environment,
            DataNode<Name> originalNode,
            DataNode<Attributes> expectedNode) {
        var diagnostics = new ErrorCollector();
        var varSupply = new UnsolvedVariableSupply();
        var kindchecker = new Kindchecker(diagnostics, environment, varSupply);
        var kindcheckedNode = kindchecker.kindcheck(originalNode);
        assertThat(diagnostics.getDiagnostics(), is(empty()));
        assertThat(kindcheckedNode, is(equalTo(expectedNode)));
    }

    void testSuccessfulKindcheck(
            TypeEnvironment environment,
            TypeNode<Name> originalNode,
            TypeNode<Attributes> expectedNode) {
        var diagnostics = new ErrorCollector();
        var varSupply = new UnsolvedVariableSupply();
        var kindchecker = new Kindchecker(diagnostics, environment, varSupply);
        var kindcheckedNode = kindchecker.kindcheck(originalNode);
        assertThat(diagnostics.getDiagnostics(), is(empty()));
        assertThat(kindcheckedNode, is(equalTo(expectedNode)));
    }

    ErrorCollector testFailedKindcheck(
            TypeEnvironment environment,
            DataNode<Name> originalNode) {
        var diagnostics = new ErrorCollector();
        var varSupply = new UnsolvedVariableSupply();
        var kindchecker = new Kindchecker(diagnostics, environment, varSupply);
        kindchecker.kindcheck(originalNode);
        var errors = diagnostics.getErrors();
        assertThat("There should be kind errors", errors, is(not(empty())));
        return diagnostics;
    }

    ErrorCollector testFailedKindcheck(
            TypeEnvironment environment,
            TypeNode<Name> originalNode) {
        var diagnostics = new ErrorCollector();
        var varSupply = new UnsolvedVariableSupply();
        var kindchecker = new Kindchecker(diagnostics, environment, varSupply);
        kindchecker.kindcheck(originalNode);
        var errors = diagnostics.getErrors();
        assertThat("There should be kind errors", errors, is(not(empty())));
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
    void kindcheckListTypeApply() {
        var environment = TypeEnvironment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.putType(ExampleNodes.List.NAME.localName(), ExampleNodes.List.KINDED_META);
        environment.putType(ExampleNodes.List.NAME.canonicalName(), ExampleNodes.List.KINDED_META);

        /*- List[Int] */
        var originalNode = typeApplyNode(
                ExampleNodes.namelessMeta(),
                ExampleNodes.List.NAMED_TYPE_NODE,
                Lists.immutable.of(ExampleNodes.Int.NAMED_TYPE_NODE));

        var expectedNode = typeApplyNode(
                ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                ExampleNodes.List.KINDED_TYPE_NODE,
                Lists.immutable.of(ExampleNodes.Int.KINDED_TYPE_NODE));

        testSuccessfulKindcheck(environment, originalNode, expectedNode);
    }

    @Test
    void kindcheckIllKindedListTypeApply() {
        var applyRange = new Range(0, 1, 0, 1);

        var environment = TypeEnvironment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.putType(ExampleNodes.List.NAME.localName(), ExampleNodes.List.KINDED_META);
        environment.putType(ExampleNodes.List.NAME.canonicalName(), ExampleNodes.List.KINDED_META);

        /*- List[Int, String] */
        var originalNode = typeApplyNode(
                new Meta<Name>(applyRange, Nameless.INSTANCE),
                ExampleNodes.List.NAMED_TYPE_NODE,
                Lists.immutable.of(
                        ExampleNodes.Int.NAMED_TYPE_NODE,
                        ExampleNodes.String.NAMED_TYPE_NODE));

        var collector = testFailedKindcheck(environment, originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                applyRange,
                "Mismatched type application! Expected: * => *, Actual: [*, *] => *");
    }

    @Test
    void kindcheckEitherTypeApply() {
        var environment = TypeEnvironment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.putType(ExampleNodes.Either.NAME.localName(), ExampleNodes.Either.KINDED_META);
        environment.putType(ExampleNodes.Either.NAME.canonicalName(), ExampleNodes.Either.KINDED_META);

        /*- Either[Int, String] */
        var originalNode = typeApplyNode(
                ExampleNodes.namelessMeta(),
                ExampleNodes.Either.NAMED_TYPE_NODE,
                Lists.immutable.of(
                        ExampleNodes.Int.NAMED_TYPE_NODE,
                        ExampleNodes.String.NAMED_TYPE_NODE));

        var expectedNode = typeApplyNode(
                ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                ExampleNodes.Either.KINDED_TYPE_NODE,
                Lists.immutable.of(
                        ExampleNodes.Int.KINDED_TYPE_NODE,
                        ExampleNodes.String.KINDED_TYPE_NODE));

        testSuccessfulKindcheck(environment, originalNode, expectedNode);
    }

    @Test
    void kindcheckIllKindedEitherTypeApply() {
        var applyRange = new Range(0, 1, 0, 1);

        var environment = TypeEnvironment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.putType(ExampleNodes.Either.NAME.localName(), ExampleNodes.Either.KINDED_META);
        environment.putType(ExampleNodes.Either.NAME.canonicalName(), ExampleNodes.Either.KINDED_META);

        /*- Either[Int] */
        var originalNode = typeApplyNode(
                new Meta<Name>(applyRange, Nameless.INSTANCE),
                ExampleNodes.Either.NAMED_TYPE_NODE,
                Lists.immutable.of(
                        ExampleNodes.Int.NAMED_TYPE_NODE));

        var collector = testFailedKindcheck(environment, originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                applyRange,
                "Mismatched type application! Expected: [*, *] => *, Actual: * => *");
    }

    @Test
    void kindcheckIllKindedIntTypeApply() {
        var applyRange = new Range(0, 1, 0, 1);

        /* Int[String] */
        var originalNode = typeApplyNode(
                new Meta<Name>(applyRange, Nameless.INSTANCE),
                ExampleNodes.Int.NAMED_TYPE_NODE,
                Lists.immutable.of(ExampleNodes.String.NAMED_TYPE_NODE));

        var collector = testFailedKindcheck(TypeEnvironment.withBuiltInTypes(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                applyRange,
                "Mismatched type application! Expected: *, Actual: * => B1");
    }

    @Test
    void kindcheckFunType() {
        /*- (Int, String) -> String */
        var originalNode = funTypeNode(
                ExampleNodes.namelessMeta(),
                Lists.immutable.of(
                        ExampleNodes.Int.NAMED_TYPE_NODE,
                        ExampleNodes.String.NAMED_TYPE_NODE),
                ExampleNodes.String.NAMED_TYPE_NODE);

        var expectedNode = funTypeNode(
                ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                Lists.immutable.of(
                        ExampleNodes.Int.KINDED_TYPE_NODE,
                        ExampleNodes.String.KINDED_TYPE_NODE),
                ExampleNodes.String.KINDED_TYPE_NODE);

        testSuccessfulKindcheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    void kindcheckIllKindedFunArgType() {
        var funArgRange = new Range(0, 1, 0, 1);

        var environment = TypeEnvironment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.putType(ExampleNodes.Either.NAME.localName(), ExampleNodes.Either.KINDED_META);
        environment.putType(ExampleNodes.Either.NAME.canonicalName(), ExampleNodes.Either.KINDED_META);

        var eitherApplyMeta = Meta
                .<Name>of(ExampleNodes.Either.NAME)
                .withRange(funArgRange);

        /*- (Int, Either) -> String */
        var originalNode = funTypeNode(
                ExampleNodes.namelessMeta(),
                Lists.immutable.of(
                        ExampleNodes.Int.NAMED_TYPE_NODE,
                        typeRefNode(eitherApplyMeta, "Either")),
                ExampleNodes.String.NAMED_TYPE_NODE);

        var collector = testFailedKindcheck(environment, originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                funArgRange,
                "Mismatched kind! Expected: *, Actual: [*, *] => *");
    }

    @Test
    void kindcheckIllKindedFunReturnType() {
        var funReturnRange = new Range(0, 1, 0, 1);

        var environment = TypeEnvironment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.putType(ExampleNodes.List.NAME.localName(), ExampleNodes.List.KINDED_META);
        environment.putType(ExampleNodes.List.NAME.canonicalName(), ExampleNodes.List.KINDED_META);

        var listReturnMeta = Meta
                .<Name>of(ExampleNodes.List.NAME)
                .withRange(funReturnRange);

        /* (Int, String) -> List */
        var originalNode = funTypeNode(
                ExampleNodes.namelessMeta(),
                Lists.immutable.of(
                        ExampleNodes.Int.NAMED_TYPE_NODE,
                        ExampleNodes.String.NAMED_TYPE_NODE),
                typeRefNode(listReturnMeta, "List"));

        var collector = testFailedKindcheck(environment, originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                funReturnRange,
                "Mismatched kind! Expected: *, Actual: * => *");
    }

    @Test
    void kindcheckTypeLambda() {
        var typeVarFName = new TypeVarName("F");
        var typeVarAName = new TypeVarName("A");

        // * => *
        var typeVarFKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        // *
        var typeVarAKind = TypeKind.INSTANCE;

        // [* => *, *] => *
        var typeLambdaKind = new HigherKind(typeVarFKind, typeVarAKind, TypeKind.INSTANCE);

        var typeVarFMeta = Meta.of(new Attributes(typeVarFName, typeVarFKind));
        var typeVarAMeta = Meta.of(new Attributes(typeVarAName, typeVarAKind));

        var typeLambdaMeta = ExampleNodes.namelessMeta(typeLambdaKind);

        // [F, A] => F[A]
        var originalNode = typeLambdaNode(
                ExampleNodes.namelessMeta(),
                Lists.immutable.of(
                        forAllVarNode(Meta.of(typeVarFName), "F"),
                        forAllVarNode(Meta.of(typeVarAName), "A")),
                typeApplyNode(
                        ExampleNodes.namelessMeta(),
                        typeRefNode(Meta.of(typeVarFName), "F"),
                        Lists.immutable.of(
                                typeRefNode(Meta.of(typeVarAName), "A"))));

        var expectedNode = typeLambdaNode(
                typeLambdaMeta,
                Lists.immutable.of(
                        forAllVarNode(typeVarFMeta, "F"),
                        forAllVarNode(typeVarAMeta, "A")),
                typeApplyNode(
                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                        typeRefNode(typeVarFMeta, "F"),
                        Lists.immutable.of(typeRefNode(typeVarAMeta, "A"))));

        testSuccessfulKindcheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    void kindcheckComplexTypeLambda() {
        var typeVarFName = new TypeVarName("F");
        var typeVarGName = new TypeVarName("G");
        var typeVarAName = new TypeVarName("A");

        // * => *
        var typeVarFKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        // * => *
        var typeVarGKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        // *
        var typeVarAKind = TypeKind.INSTANCE;

        // [* => *, * => *, *] => *
        var typeLambdaKind = new HigherKind(typeVarFKind, typeVarGKind, typeVarAKind, TypeKind.INSTANCE);

        var typeVarFMeta = Meta.of(new Attributes(typeVarFName, typeVarFKind));
        var typeVarGMeta = Meta.of(new Attributes(typeVarGName, typeVarGKind));
        var typeVarAMeta = Meta.of(new Attributes(typeVarAName, typeVarAKind));

        var typeLambdaMeta = ExampleNodes.namelessMeta(typeLambdaKind);

        // [F, G, A] => F[G[A]]
        var originalNode = typeLambdaNode(
                ExampleNodes.namelessMeta(),
                Lists.immutable.of(
                        forAllVarNode(Meta.of(typeVarFName), "F"),
                        forAllVarNode(Meta.of(typeVarGName), "G"),
                        forAllVarNode(Meta.of(typeVarAName), "A")),
                typeApplyNode(
                        ExampleNodes.namelessMeta(),
                        typeRefNode(Meta.of(typeVarFName), "F"),
                        Lists.immutable.of(
                                typeApplyNode(
                                        ExampleNodes.namelessMeta(),
                                        typeRefNode(Meta.of(typeVarGName), "G"),
                                        Lists.immutable.of(typeRefNode(Meta.of(typeVarAName), "A"))))));

        var expectedNode = typeLambdaNode(
                typeLambdaMeta,
                Lists.immutable.of(
                        forAllVarNode(typeVarFMeta, "F"),
                        forAllVarNode(typeVarGMeta, "G"),
                        forAllVarNode(typeVarAMeta, "A")),
                typeApplyNode(
                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                        typeRefNode(typeVarFMeta, "F"),
                        Lists.immutable.of(
                                typeApplyNode(
                                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                                        typeRefNode(typeVarGMeta, "G"),
                                        Lists.immutable.of(typeRefNode(typeVarAMeta, "A"))))));

        testSuccessfulKindcheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    void kindcheckTypeLambdaWithComplexFunctionType() {
        var typeVarFName = new TypeVarName("F");
        var typeVarGName = new TypeVarName("G");
        var typeVarAName = new TypeVarName("A");
        var typeVarBName = new TypeVarName("B");

        // * => *
        var typeVarGKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        // [* => *, *] => *
        var typeVarFKind = new HigherKind(typeVarGKind, TypeKind.INSTANCE, TypeKind.INSTANCE);
        // *
        var typeVarAKind = TypeKind.INSTANCE;
        // *
        var typeVarBKind = TypeKind.INSTANCE;

        // [* => *, [* => *, *] => *, *, *] => *
        var typeLambdaKind = new HigherKind(
                typeVarFKind, typeVarGKind, typeVarAKind, typeVarBKind,
                TypeKind.INSTANCE);

        var typeVarFMeta = Meta.of(new Attributes(typeVarFName, typeVarFKind));
        var typeVarGMeta = Meta.of(new Attributes(typeVarGName, typeVarGKind));
        var typeVarAMeta = Meta.of(new Attributes(typeVarAName, typeVarAKind));
        var typeVarBMeta = Meta.of(new Attributes(typeVarBName, typeVarBKind));

        var typeLambdaMeta = ExampleNodes.namelessMeta(typeLambdaKind);

        // [F, G, A, B] => F[G, A] -> F[G, B] -> G[B]
        var originalNode = typeLambdaNode(
                ExampleNodes.namelessMeta(),
                Lists.immutable.of(
                        forAllVarNode(Meta.of(typeVarFName), "F"),
                        forAllVarNode(Meta.of(typeVarGName), "G"),
                        forAllVarNode(Meta.of(typeVarAName), "A"),
                        forAllVarNode(Meta.of(typeVarBName), "B")),
                funTypeNode(
                        ExampleNodes.namelessMeta(),
                        Lists.immutable.of(
                                typeApplyNode(
                                        ExampleNodes.namelessMeta(),
                                        typeRefNode(Meta.of(typeVarFName), "F"),
                                        Lists.immutable.of(
                                                typeRefNode(Meta.of(typeVarGName), "G"),
                                                typeRefNode(Meta.of(typeVarAName), "A")))),
                        funTypeNode(
                                ExampleNodes.namelessMeta(),
                                Lists.immutable.of(
                                        typeApplyNode(
                                                ExampleNodes.namelessMeta(),
                                                typeRefNode(Meta.of(typeVarFName), "F"),
                                                Lists.immutable.of(
                                                        typeRefNode(Meta.of(typeVarGName), "G"),
                                                        typeRefNode(Meta.of(typeVarBName), "B")))),
                                typeApplyNode(
                                        ExampleNodes.namelessMeta(),
                                        typeRefNode(Meta.of(typeVarGName), "G"),
                                        Lists.immutable.of(typeRefNode(Meta.of(typeVarBName), "B"))))));

        var expectedNode = typeLambdaNode(
                typeLambdaMeta,
                Lists.immutable.of(
                        forAllVarNode(typeVarFMeta, "F"),
                        forAllVarNode(typeVarGMeta, "G"),
                        forAllVarNode(typeVarAMeta, "A"),
                        forAllVarNode(typeVarBMeta, "B")),
                funTypeNode(
                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                        Lists.immutable.of(
                                typeApplyNode(
                                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                                        typeRefNode(typeVarFMeta, "F"),
                                        Lists.immutable.of(
                                                typeRefNode(typeVarGMeta, "G"),
                                                typeRefNode(typeVarAMeta, "A")))),
                        funTypeNode(
                                ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                                Lists.immutable.of(
                                        typeApplyNode(
                                                ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                                                typeRefNode(typeVarFMeta, "F"),
                                                Lists.immutable.of(
                                                        typeRefNode(typeVarGMeta, "G"),
                                                        typeRefNode(typeVarBMeta, "B")))),
                                typeApplyNode(
                                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                                        typeRefNode(typeVarGMeta, "G"),
                                        Lists.immutable.of(typeRefNode(typeVarBMeta, "B"))))));

        testSuccessfulKindcheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    void kindcheckHigherTyCon() {
        var environment = TypeEnvironment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.putType(ExampleNodes.List.NAME.localName(), ExampleNodes.List.KINDED_META);
        environment.putType(ExampleNodes.List.NAME.canonicalName(), ExampleNodes.List.KINDED_META);
        environment.putType(ExampleNodes.Functor.NAME.localName(), ExampleNodes.Functor.KINDED_META);
        environment.putType(ExampleNodes.Functor.NAME.canonicalName(), ExampleNodes.Functor.KINDED_META);

        // Functor[List]
        var originalNode = typeApplyNode(
                Meta.of(Nameless.INSTANCE),
                ExampleNodes.Functor.NAMED_TYPE_NODE,
                Lists.immutable.of(ExampleNodes.List.NAMED_TYPE_NODE));

        var expectedNode = typeApplyNode(
                ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                ExampleNodes.Functor.KINDED_TYPE_NODE,
                Lists.immutable.of(ExampleNodes.List.KINDED_TYPE_NODE));

        testSuccessfulKindcheck(environment, originalNode, expectedNode);
    }

    @Test
    void kindcheckEtaExpandHigherTyCon() {
        var environment = TypeEnvironment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.putType(ExampleNodes.Functor.NAME.localName(), ExampleNodes.Functor.KINDED_META);
        environment.putType(ExampleNodes.Functor.NAME.canonicalName(), ExampleNodes.Functor.KINDED_META);

        // * => *
        var typeVarFKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        var typeVarFName = new TypeVarName("F");
        var typeVarFMeta = Meta.of(new Attributes(typeVarFName, typeVarFKind));

        // F => Functor[F]
        var originalNode = typeLambdaNode(
                ExampleNodes.namelessMeta(),
                Lists.immutable.of(forAllVarNode(Meta.of(typeVarFName), "F")),
                typeApplyNode(
                        ExampleNodes.namelessMeta(),
                        ExampleNodes.Functor.NAMED_TYPE_NODE,
                        Lists.immutable.of(typeRefNode(Meta.of(typeVarFName), "F"))));

        var expectedNode = typeLambdaNode(
                ExampleNodes.namelessMeta(ExampleNodes.Functor.KIND),
                Lists.immutable.of(forAllVarNode(typeVarFMeta, "F")),
                typeApplyNode(
                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                        ExampleNodes.Functor.KINDED_TYPE_NODE,
                        Lists.immutable.of(typeRefNode(typeVarFMeta, "F"))));

        testSuccessfulKindcheck(environment, originalNode, expectedNode);
    }

    @Test
    void kindcheckHigherTyConTypeLambda() {
        var environment = TypeEnvironment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.putType(ExampleNodes.Functor.NAME.localName(), ExampleNodes.Functor.KINDED_META);
        environment.putType(ExampleNodes.Functor.NAME.canonicalName(), ExampleNodes.Functor.KINDED_META);

        // [* => *] => *
        var typeVarFKind = new HigherKind(ExampleNodes.Functor.KIND, TypeKind.INSTANCE);
        var typeVarFName = new TypeVarName("F");
        var typeVarFMeta = Meta.of(new Attributes(typeVarFName, typeVarFKind));

        var typeLambdaKind = new HigherKind(typeVarFKind, TypeKind.INSTANCE);

        // F => F[Functor]
        var originalNode = typeLambdaNode(
                ExampleNodes.namelessMeta(),
                Lists.immutable.of(forAllVarNode(Meta.of(typeVarFName), "F")),
                typeApplyNode(
                        ExampleNodes.namelessMeta(),
                        typeRefNode(Meta.of(typeVarFName), "F"),
                        Lists.immutable.of(ExampleNodes.Functor.NAMED_TYPE_NODE)));

        var expectedNode = typeLambdaNode(
                ExampleNodes.namelessMeta(typeLambdaKind),
                Lists.immutable.of(forAllVarNode(typeVarFMeta, "F")),
                typeApplyNode(
                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                        typeRefNode(typeVarFMeta, "F"),
                        Lists.immutable.of(ExampleNodes.Functor.KINDED_TYPE_NODE)));

        testSuccessfulKindcheck(environment, originalNode, expectedNode);
    }

    @Test
    void kindcheckBinaryTyConAdaptedToUnary() {
        var environment = TypeEnvironment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.putType(ExampleNodes.Either.NAME.localName(), ExampleNodes.Either.KINDED_META);
        environment.putType(ExampleNodes.Either.NAME.canonicalName(), ExampleNodes.Either.KINDED_META);
        environment.putType(ExampleNodes.Functor.NAME.localName(), ExampleNodes.Functor.KINDED_META);
        environment.putType(ExampleNodes.Functor.NAME.canonicalName(), ExampleNodes.Functor.KINDED_META);

        var typeVarAName = new TypeVarName("A");
        var typeVarAKind = TypeKind.INSTANCE;
        var typeVarAMeta = Meta.of(new Attributes(typeVarAName, typeVarAKind));

        // Functor[A => Either[String, A]]
        var originalNode = typeApplyNode(
                ExampleNodes.namelessMeta(),
                ExampleNodes.Functor.NAMED_TYPE_NODE,
                Lists.immutable.of(
                        typeLambdaNode(
                                ExampleNodes.namelessMeta(),
                                Lists.immutable.of(forAllVarNode(Meta.of(typeVarAName), "A")),
                                typeApplyNode(
                                        ExampleNodes.namelessMeta(),
                                        ExampleNodes.Either.NAMED_TYPE_NODE,
                                        Lists.immutable.of(
                                                ExampleNodes.String.NAMED_TYPE_NODE,
                                                typeRefNode(Meta.of(typeVarAName), "A"))))));

        var expectedNode = typeApplyNode(
                ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                ExampleNodes.Functor.KINDED_TYPE_NODE,
                Lists.immutable.of(
                        typeLambdaNode(
                                ExampleNodes.namelessMeta(new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE)),
                                Lists.immutable.of(forAllVarNode(typeVarAMeta, "A")),
                                typeApplyNode(
                                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                                        ExampleNodes.Either.KINDED_TYPE_NODE,
                                        Lists.immutable.of(
                                                ExampleNodes.String.KINDED_TYPE_NODE,
                                                typeRefNode(typeVarAMeta, "A"))))));

        testSuccessfulKindcheck(environment, originalNode, expectedNode);
    }

    @Test
    void kindcheckListDataType() {
        var typeVarAName = (Name) new TypeVarName("A");
        var typeVarAKind = TypeKind.INSTANCE;
        var typeVarAMeta = Meta.of(new Attributes(typeVarAName, typeVarAKind));

        /*-
         * data List[A] {
         *   case Cons(head: A)
         *   case Nil()
         * }
         */
        var originalNode = dataNode(
                Meta.<Name>of(ExampleNodes.List.NAME),
                "List",
                Lists.immutable.of(
                        forAllVarNode(Meta.of(typeVarAName), "A")),
                Lists.immutable.of(
                        constructorNode(
                                Meta.<Name>of(ExampleNodes.Cons.NAME),
                                "Cons",
                                Lists.immutable.of(
                                        constructorParamNode(
                                                Meta.<Name>of(ExampleNodes.Cons.HEAD_NAME),
                                                "head",
                                                typeRefNode(Meta.of(typeVarAName), "A"))),
                                Optional.empty()),
                        constructorNode(
                                Meta.<Name>of(ExampleNodes.Nil.NAME),
                                "Nil",
                                Lists.immutable.empty(),
                                Optional.empty())));

        var expectedNode = dataNode(
                ExampleNodes.List.KINDED_META,
                "List",
                Lists.immutable.of(
                        forAllVarNode(typeVarAMeta, "A")),
                Lists.immutable.of(
                        constructorNode(
                                ExampleNodes.Cons.KINDED_META,
                                "Cons",
                                Lists.immutable.of(
                                        constructorParamNode(
                                                ExampleNodes.Cons.KINDED_HEAD_META,
                                                "head",
                                                typeRefNode(typeVarAMeta, "A"))),
                                Optional.empty()),
                        constructorNode(
                                ExampleNodes.Nil.KINDED_META,
                                "Nil",
                                Lists.immutable.empty(),
                                Optional.empty())));

        testSuccessfulKindcheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void kindcheckFixDataType() {
        var typeVarFName = (Name) new TypeVarName("F");
        var typeVarFKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        var typeVarFMeta = Meta.of(new Attributes(typeVarFName, typeVarFKind));

        /*-
         * data Fix[F] { case Unfix(unfix: F[Fix[F]]) }
         */
        var originalNode = dataNode(
                Meta.<Name>of(ExampleNodes.Fix.NAME),
                "Fix",
                Lists.immutable.of(
                        forAllVarNode(Meta.of(typeVarFName), "F")),
                Lists.immutable.of(
                        constructorNode(
                                Meta.<Name>of(ExampleNodes.Unfix.NAME),
                                "Unfix",
                                Lists.immutable.of(
                                        constructorParamNode(
                                                Meta.<Name>of(ExampleNodes.Unfix.UNFIX_NAME),
                                                "unfix",
                                                typeApplyNode(
                                                        ExampleNodes.namelessMeta(),
                                                        typeRefNode(Meta.of(typeVarFName), "F"),
                                                        Lists.immutable.of(
                                                                typeApplyNode(
                                                                        ExampleNodes.namelessMeta(),
                                                                        ExampleNodes.Fix.NAMED_TYPE_NODE,
                                                                        Lists.immutable.of(
                                                                                typeRefNode(Meta.of(typeVarFName),
                                                                                        "F"))))))),
                                Optional.empty())));

        var expectedNode = dataNode(
                ExampleNodes.Fix.KINDED_META,
                "Fix",
                Lists.immutable.of(
                        forAllVarNode(typeVarFMeta, "F")),
                Lists.immutable.of(
                        constructorNode(
                                ExampleNodes.Unfix.KINDED_META,
                                "Unfix",
                                Lists.immutable.of(
                                        constructorParamNode(
                                                ExampleNodes.Unfix.KINDED_UNFIX_META,
                                                "unfix",
                                                typeApplyNode(
                                                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                                                        typeRefNode(typeVarFMeta, "F"),
                                                        Lists.immutable.of(
                                                                typeApplyNode(
                                                                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                                                                        ExampleNodes.Fix.KINDED_TYPE_NODE,
                                                                        Lists.immutable.of(typeRefNode(typeVarFMeta, "F"))))))),
                                Optional.empty())));

        testSuccessfulKindcheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    @Property
    void kindcheckBuiltInTypes(@ForAll("builtIns") Map.Entry<String, Meta<Attributes>> builtIn) {
        var originalRange = new Range(0, 1, 0, 1);

        var builtInName = builtIn.getKey();
        var builtInMeta = builtIn.getValue().withRange(originalRange);

        var metaWithName = builtInMeta.withMeta(builtInMeta.meta().name());

        var originalNode = typeRefNode(metaWithName, builtInName);

        var expectedNode = typeRefNode(builtInMeta, builtInName);

        testSuccessfulKindcheck(TypeEnvironment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Provide
    Arbitrary<Map.Entry<String, Meta<Attributes>>> builtIns() {
        return Arbitraries.of(BuiltInScope.withBuiltInTypes().types().entrySet());
    }
}

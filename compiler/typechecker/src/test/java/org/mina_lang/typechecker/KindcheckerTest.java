/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.typechecker;

import com.opencastsoftware.yvette.Range;
import net.jqwik.api.*;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.diagnostics.Diagnostic;
import org.mina_lang.common.diagnostics.NamespaceDiagnosticReporter;
import org.mina_lang.common.names.ForAllVarName;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.names.Nameless;
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.DataNode;
import org.mina_lang.syntax.MetaNodeSubstitutionTransformer;
import org.mina_lang.syntax.TypeNode;
import org.mina_lang.typechecker.scopes.BuiltInTypingScope;
import org.mina_lang.typechecker.scopes.ImportedTypesScope;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mina_lang.syntax.SyntaxNodes.*;

public class KindcheckerTest {
    void testSuccessfulKindcheck(
            TypeEnvironment environment,
            DataNode<Name> originalNode,
            DataNode<Attributes> expectedNode) {
        var baseCollector = new ErrorCollector();
        var varSupply = new UnsolvedVariableSupply();
        var sortTransformer = new SortSubstitutionTransformer(
            environment.typeSubstitution(), environment.kindSubstitution());
        var dummyUri = URI.create("file:///Mina/Test/Kindchecker.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var kindchecker = new Kindchecker(scopedCollector, environment, varSupply, sortTransformer);
        var kindDefaultingTransformer = new SortSubstitutionTransformer(
                environment.typeSubstitution(),
                new KindDefaultingTransformer(environment.kindSubstitution()));
        var kindcheckedNode = kindchecker.kindcheck(originalNode)
                .accept(new MetaNodeSubstitutionTransformer(kindDefaultingTransformer));
        assertThat(baseCollector.getDiagnostics(), is(empty()));
        assertThat(kindcheckedNode, is(equalTo(expectedNode)));
    }

    void testSuccessfulKindcheck(
            TypeEnvironment environment,
            TypeNode<Name> originalNode,
            TypeNode<Attributes> expectedNode) {
        var baseCollector = new ErrorCollector();
        var varSupply = new UnsolvedVariableSupply();
        var sortTransformer = new SortSubstitutionTransformer(
                environment.typeSubstitution(), environment.kindSubstitution());
        var dummyUri = URI.create("file:///Mina/Test/Kindchecker.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var kindchecker = new Kindchecker(scopedCollector, environment, varSupply, sortTransformer);
        var kindcheckedNode = kindchecker.kindcheck(originalNode);
        assertThat(baseCollector.getDiagnostics(), is(empty()));
        assertThat(kindcheckedNode, is(equalTo(expectedNode)));
    }

    ErrorCollector testFailedKindcheck(
            TypeEnvironment environment,
            DataNode<Name> originalNode) {
        var baseCollector = new ErrorCollector();
        var varSupply = new UnsolvedVariableSupply();
        var sortTransformer = new SortSubstitutionTransformer(
                environment.typeSubstitution(), environment.kindSubstitution());
        var dummyUri = URI.create("file:///Mina/Test/Kindchecker.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var kindchecker = new Kindchecker(scopedCollector, environment, varSupply, sortTransformer);
        kindchecker.kindcheck(originalNode);
        var errors = baseCollector.getErrors();
        assertThat("There should be kind errors", errors, is(not(empty())));
        return baseCollector;
    }

    ErrorCollector testFailedKindcheck(
            TypeEnvironment environment,
            TypeNode<Name> originalNode) {
        var baseCollector = new ErrorCollector();
        var varSupply = new UnsolvedVariableSupply();
        var sortTransformer = new SortSubstitutionTransformer(
                environment.typeSubstitution(), environment.kindSubstitution());
        var dummyUri = URI.create("file:///Mina/Test/Kindchecker.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var kindchecker = new Kindchecker(scopedCollector, environment, varSupply, sortTransformer);
        kindchecker.kindcheck(originalNode);
        var errors = baseCollector.getErrors();
        assertThat("There should be kind errors", errors, is(not(empty())));
        return baseCollector;
    }

    void assertDiagnostic(List<Diagnostic> diagnostics, Range range, String message) {
        assertThat(diagnostics, is(not(empty())));
        var firstDiagnostic = diagnostics.get(0);
        assertThat(firstDiagnostic.message(), is(equalTo(message)));
        assertThat(firstDiagnostic.location().range(), is(equalTo(range)));
        assertThat(firstDiagnostic.relatedInformation().toList(), is(empty()));
    }

    @Test
    void kindcheckListTypeApply() {
        var environment = TypeEnvironment.withBuiltInTypes();
        environment.pushScope(new ImportedTypesScope());
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
        environment.pushScope(new ImportedTypesScope());
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
        environment.pushScope(new ImportedTypesScope());
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
        environment.pushScope(new ImportedTypesScope());
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
        environment.pushScope(new ImportedTypesScope());
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
        environment.pushScope(new ImportedTypesScope());
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
    void kindcheckQuantifiedType() {
        var typeVarFName = new ForAllVarName("F");
        var typeVarAName = new ForAllVarName("A");

        // * => *
        var typeVarFKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        // *
        var typeVarAKind = TypeKind.INSTANCE;

        var typeVarFMeta = Meta.of(typeVarFName, typeVarFKind);
        var typeVarAMeta = Meta.of(typeVarAName, typeVarAKind);

        // [F, A] { F[A] }
        var originalNode = quantifiedTypeNode(
                ExampleNodes.namelessMeta(),
                Lists.immutable.of(
                        forAllVarNode(Meta.of(typeVarFName), "F"),
                        forAllVarNode(Meta.of(typeVarAName), "A")),
                typeApplyNode(
                        ExampleNodes.namelessMeta(),
                        typeRefNode(Meta.of(typeVarFName), "F"),
                        Lists.immutable.of(
                                typeRefNode(Meta.of(typeVarAName), "A"))));

        var expectedNode = quantifiedTypeNode(
                ExampleNodes.namelessMeta(TypeKind.INSTANCE),
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
    void kindcheckComplexQuantifiedType() {
        var typeVarFName = new ForAllVarName("F");
        var typeVarGName = new ForAllVarName("G");
        var typeVarAName = new ForAllVarName("A");

        // * => *
        var typeVarFKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        // * => *
        var typeVarGKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        // *
        var typeVarAKind = TypeKind.INSTANCE;

        var typeVarFMeta = Meta.of(typeVarFName, typeVarFKind);
        var typeVarGMeta = Meta.of(typeVarGName, typeVarGKind);
        var typeVarAMeta = Meta.of(typeVarAName, typeVarAKind);

        // [F, G, A] { F[G[A]] }
        var originalNode = quantifiedTypeNode(
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

        var expectedNode = quantifiedTypeNode(
                ExampleNodes.namelessMeta(TypeKind.INSTANCE),
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
    void kindcheckComplexQuantifiedFunctionType() {
        var typeVarFName = new ForAllVarName("F");
        var typeVarGName = new ForAllVarName("G");
        var typeVarAName = new ForAllVarName("A");
        var typeVarBName = new ForAllVarName("B");

        // * => *
        var typeVarGKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        // [* => *, *] => *
        var typeVarFKind = new HigherKind(typeVarGKind, TypeKind.INSTANCE, TypeKind.INSTANCE);
        // *
        var typeVarAKind = TypeKind.INSTANCE;
        // *
        var typeVarBKind = TypeKind.INSTANCE;

        var typeVarFMeta = Meta.of(typeVarFName, typeVarFKind);
        var typeVarGMeta = Meta.of(typeVarGName, typeVarGKind);
        var typeVarAMeta = Meta.of(typeVarAName, typeVarAKind);
        var typeVarBMeta = Meta.of(typeVarBName, typeVarBKind);

        // [F, G, A, B] { F[G, A] -> F[G, B] -> G[B] }
        var originalNode = quantifiedTypeNode(
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

        var expectedNode = quantifiedTypeNode(
                ExampleNodes.namelessMeta(TypeKind.INSTANCE),
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
        environment.pushScope(new ImportedTypesScope());
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
        environment.pushScope(new ImportedTypesScope());
        environment.putType(ExampleNodes.Functor.NAME.localName(), ExampleNodes.Functor.KINDED_META);
        environment.putType(ExampleNodes.Functor.NAME.canonicalName(), ExampleNodes.Functor.KINDED_META);

        // * => *
        var typeVarFKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        var typeVarFName = new ForAllVarName("F");
        var typeVarFMeta = Meta.of(typeVarFName, typeVarFKind);

        // [F] { Functor[F] }
        var originalNode = quantifiedTypeNode(
                ExampleNodes.namelessMeta(),
                Lists.immutable.of(forAllVarNode(Meta.of(typeVarFName), "F")),
                typeApplyNode(
                        ExampleNodes.namelessMeta(),
                        ExampleNodes.Functor.NAMED_TYPE_NODE,
                        Lists.immutable.of(typeRefNode(Meta.of(typeVarFName), "F"))));

        var expectedNode = quantifiedTypeNode(
                ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                Lists.immutable.of(forAllVarNode(typeVarFMeta, "F")),
                typeApplyNode(
                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                        ExampleNodes.Functor.KINDED_TYPE_NODE,
                        Lists.immutable.of(typeRefNode(typeVarFMeta, "F"))));

        testSuccessfulKindcheck(environment, originalNode, expectedNode);
    }

    @Test
    void kindcheckQuantifiedHigherKindedTypeConstructorType() {
        var environment = TypeEnvironment.withBuiltInTypes();
        environment.pushScope(new ImportedTypesScope());
        environment.putType(ExampleNodes.Functor.NAME.localName(), ExampleNodes.Functor.KINDED_META);
        environment.putType(ExampleNodes.Functor.NAME.canonicalName(), ExampleNodes.Functor.KINDED_META);

        // [* => *] => *
        var typeVarFKind = new HigherKind(ExampleNodes.Functor.KIND, TypeKind.INSTANCE);
        var typeVarFName = new ForAllVarName("F");
        var typeVarFMeta = Meta.of(typeVarFName, typeVarFKind);

        // F { F[Functor] }
        var originalNode = quantifiedTypeNode(
                ExampleNodes.namelessMeta(),
                Lists.immutable.of(forAllVarNode(Meta.of(typeVarFName), "F")),
                typeApplyNode(
                        ExampleNodes.namelessMeta(),
                        typeRefNode(Meta.of(typeVarFName), "F"),
                        Lists.immutable.of(ExampleNodes.Functor.NAMED_TYPE_NODE)));

        var expectedNode = quantifiedTypeNode(
                ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                Lists.immutable.of(forAllVarNode(typeVarFMeta, "F")),
                typeApplyNode(
                        ExampleNodes.namelessMeta(TypeKind.INSTANCE),
                        typeRefNode(typeVarFMeta, "F"),
                        Lists.immutable.of(ExampleNodes.Functor.KINDED_TYPE_NODE)));

        testSuccessfulKindcheck(environment, originalNode, expectedNode);
    }

    @Test
    void kindcheckListDataType() {
        /*-
         * data List[A] {
         *   case Cons(head: A)
         *   case Nil()
         * }
         */
        var originalNode = ExampleNodes.List.NAMED_NODE;

        var expectedNode = ExampleNodes.List.KINDED_NODE;

        testSuccessfulKindcheck(TypeEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void kindcheckFixDataType() {
        /*-
         * data Fix[F] { case Unfix(unfix: F[Fix[F]]) }
         */
        var originalNode = ExampleNodes.Fix.NAMED_NODE;

        var expectedNode = ExampleNodes.Fix.KINDED_NODE;

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
        return Arbitraries.of(BuiltInTypingScope.empty().types().entrySet());
    }
}

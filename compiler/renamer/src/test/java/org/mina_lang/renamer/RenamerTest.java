/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.renamer;

import com.opencastsoftware.yvette.Range;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mina_lang.common.Meta;
import org.mina_lang.common.diagnostics.Diagnostic;
import org.mina_lang.common.diagnostics.NamespaceDiagnosticReporter;
import org.mina_lang.common.names.*;
import org.mina_lang.renamer.scopes.ImportedNamesScope;
import org.mina_lang.syntax.MetaNode;
import org.mina_lang.syntax.NamespaceNode;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mina_lang.syntax.SyntaxNodes.*;

public class RenamerTest {

    void testSuccessfulRename(
            NameEnvironment environment,
            NamespaceNode<Void> originalNode,
            NamespaceNode<Name> expectedNode) {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Renamer.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var renamer = new Renamer(scopedCollector, environment);
        var renamedNode = renamer.rename(originalNode);
        assertThat(baseCollector.getDiagnostics(), is(empty()));
        assertThat(renamedNode, is(equalTo(expectedNode)));
    }

    ErrorCollector testRenameWithWarnings(
        NameEnvironment environment,
        NamespaceNode<Void> originalNode,
        NamespaceNode<Name> expectedNode) {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Renamer.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var renamer = new Renamer(scopedCollector, environment);
        var renamedNode = renamer.rename(originalNode);
        assertThat(renamedNode, is(equalTo(expectedNode)));
        return baseCollector;
    }

    <A extends Name> void testSuccessfulRename(
            NameEnvironment environment,
            MetaNode<Void> originalNode,
            MetaNode<A> expectedNode) {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Renamer.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var renamer = new Renamer(scopedCollector, environment);
        var renamedNode = renamer.rename(originalNode);
        assertThat(baseCollector.getDiagnostics(), is(empty()));
        assertThat(renamedNode, is(equalTo(expectedNode)));
    }

    ErrorCollector testFailedRename(
            NameEnvironment environment,
            MetaNode<Void> originalNode) {
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Renamer.mina");
        var scopedCollector = new NamespaceDiagnosticReporter(baseCollector, dummyUri);
        var renamer = new Renamer(scopedCollector, environment);
        renamer.rename(originalNode);
        var errors = baseCollector.getErrors();
        assertThat("There should be renaming errors", errors, is(not(empty())));
        return baseCollector;
    }

    void assertDiagnostic(List<Diagnostic> diagnostics, Range range, String message) {
        assertThat(diagnostics, is(not(empty())));
        var firstDiagnostic = diagnostics.get(0);
        assertThat(firstDiagnostic.message(), is(equalTo(message)));
        assertThat(firstDiagnostic.location().range(), is(equalTo(range)));
        assertThat(firstDiagnostic.relatedInformation(), is(empty()));
    }

    void assertDiagnosticWithRelatedInfo(List<Diagnostic> diagnostics, Range diagnosticRange, String diagnosticMessage,
            Range relatedInfoRange, String relatedInfoMessage) {
        assertThat(diagnostics, is(not(empty())));

        var firstDiagnostic = diagnostics.get(0);
        assertThat(firstDiagnostic.message(), is(equalTo(diagnosticMessage)));
        assertThat(firstDiagnostic.location().range(), is(equalTo(diagnosticRange)));

        assertThat(firstDiagnostic.relatedInformation(), is(not(empty())));

        var firstRelatedInfo = firstDiagnostic.relatedInformation().get(0);
        assertThat(firstRelatedInfo.message(), startsWith(relatedInfoMessage));
        assertThat(firstRelatedInfo.location().range(), is(equalTo(relatedInfoRange)));
    }

    void assertDuplicateValueDefinition(List<Diagnostic> diagnostics, Range duplicateRange, Range originalRange,
            String name) {
        assertDiagnosticWithRelatedInfo(
                diagnostics,
                duplicateRange, "Duplicate definition of value '" + name + "'",
                originalRange, "Original definition of value '" + name + "'");
    }

    void assertDuplicateTypeDefinition(List<Diagnostic> diagnostics, Range duplicateRange, Range originalRange,
            String name) {
        assertDiagnosticWithRelatedInfo(
                diagnostics,
                duplicateRange, "Duplicate definition of type '" + name + "'",
                originalRange, "Original definition of type '" + name + "'");
    }

    void assertDuplicateFieldDefinition(List<Diagnostic> diagnostics, Range duplicateRange, Range originalRange,
            String name, String constr) {
        assertDiagnosticWithRelatedInfo(
                diagnostics,
                duplicateRange,
                "Duplicate definition of field '" + name + "' in constructor 'Mina/Test/Renamer." + constr + "'",
                originalRange,
                "Original definition of field '" + name + "' in constructor 'Mina/Test/Renamer." + constr + "'");
    }

    // Namespaces
    @Test
    void renameNamespace() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var namespaceName = new NamespaceName(List.of("Mina", "Test"), "Renamer");

        // namespace Mina/Test/Renamer {}
        var originalNode = namespaceNode(Range.EMPTY, idNode, List.of(),
                List.of());

        var expectedNode = namespaceNode(Meta.of(namespaceName), idNode,
                List.of(), List.of());

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    // Declarations
    @Test
    void renameEmptyData() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var namespaceName = new NamespaceName(List.of("Mina", "Test"), "Renamer");
        var dataName = new DataName(new QualifiedName(namespaceName, "Void"));

        /*-
         * namespace Mina/Test/Renamer {
         *   data Void {}
         * }
         */
        var originalNode = namespaceNode(
                Range.EMPTY, idNode, List.of(),
                List.of(
                        dataNode(Range.EMPTY, "Void", List.of(), List.of())));

        var expectedNode = namespaceNode(
                Meta.of(namespaceName), idNode, List.of(),
                List.of(dataNode(Meta.of(dataName), "Void",
                        List.of(), List.of())));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameEmptyDataDuplicateDeclaration() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var originalDataRange = new Range(1, 2, 1, 13);
        var duplicateDataRange = new Range(2, 2, 2, 13);
        /*-
         * namespace Mina/Test/Renamer {
         *   data Void {}
         *   data Void {}
         * }
         */
        var originalNode = namespaceNode(
                Range.EMPTY, idNode, List.of(),
                List.of(
                        dataNode(originalDataRange, "Void", List.of(), List.of()),
                        dataNode(duplicateDataRange, "Void", List.of(), List.of())));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDuplicateTypeDefinition(
                collector.getDiagnostics(),
                duplicateDataRange, originalDataRange, "Void");
    }

    @Test
    void renameSimpleData() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var namespaceName = new NamespaceName(List.of("Mina", "Test"), "Renamer");
        var dataName = new DataName(new QualifiedName(namespaceName, "Boolean"));
        var trueName = new ConstructorName(dataName, new QualifiedName(namespaceName, "True"));
        var falseName = new ConstructorName(dataName, new QualifiedName(namespaceName, "False"));

        /*-
         * namespace Mina/Test/Renamer {
         *   data Boolean {
         *     case True()
         *     case False()
         *   }
         * }
         */
        var originalNode = namespaceNode(
                Range.EMPTY, idNode, List.of(),
                List.of(
                        dataNode(Range.EMPTY, "Boolean", List.of(), List.of(
                                constructorNode(Range.EMPTY, "True", List.of(),
                                        Optional.empty()),
                                constructorNode(Range.EMPTY, "False", List.of(),
                                        Optional.empty())))));

        var expectedNode = namespaceNode(
                Meta.of(namespaceName), idNode, List.of(),
                List.of(
                        dataNode(Meta.of(dataName), "Boolean", List.of(),
                                List.of(
                                        constructorNode(Meta.of(trueName), "True",
                                                List.of(),
                                                Optional.empty()),
                                        constructorNode(Meta.of(falseName), "False",
                                                List.of(),
                                                Optional.empty())))));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameDataMutualRecursion() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var namespaceName = new NamespaceName(List.of("Mina", "Test"), "Renamer");

        var dataCName = new DataName(new QualifiedName(namespaceName, "C"));
        var dataBName = new DataName(new QualifiedName(namespaceName, "B"));
        var dataAName = new DataName(new QualifiedName(namespaceName, "A"));

        var mkCName = new ConstructorName(dataCName, new QualifiedName(namespaceName, "MkC"));
        var mkBName = new ConstructorName(dataBName, new QualifiedName(namespaceName, "MkB"));
        var mkAName = new ConstructorName(dataAName, new QualifiedName(namespaceName, "MkA"));

        var mkCFieldName = new FieldName(mkCName, "b");
        var mkBFieldName = new FieldName(mkBName, "a");
        var mkAFieldName = new FieldName(mkAName, "b");

        var dataCRange = new Range(1, 2, 3, 2);
        var dataBRange = new Range(4, 2, 6, 2);
        var dataARange = new Range(7, 2, 9, 2);

        /*-
         * namespace Mina/Test/Renamer {
         *   data C {
         *     case MkC(b: B)
         *   }
         *   data B {
         *     case MkB(a: A)
         *   }
         *   data A {
         *     case MkA(b: B)
         *   }
         * }
         */
        var originalNode = new NamespaceNode<>(Meta.of(Range.EMPTY), idNode, List.of(),
                List.of(
                        List.of(
                                dataNode(dataCRange, "C",
                                        List.of(),
                                        List.of(
                                                constructorNode(
                                                        Range.EMPTY, "MkC",
                                                        List.of(
                                                                constructorParamNode(Range.EMPTY, "b",
                                                                        typeRefNode(Range.EMPTY, "B"))),
                                                        Optional.empty()))),
                                dataNode(dataBRange, "B",
                                        List.of(),
                                        List.of(
                                                constructorNode(
                                                        Range.EMPTY, "MkB",
                                                        List.of(
                                                                constructorParamNode(Range.EMPTY, "a",
                                                                        typeRefNode(Range.EMPTY, "A"))),
                                                        Optional.empty()))),
                                dataNode(dataARange, "A",
                                        List.of(),
                                        List.of(
                                                constructorNode(
                                                        Range.EMPTY, "MkA",
                                                        List.of(
                                                                constructorParamNode(Range.EMPTY, "b",
                                                                        typeRefNode(Range.EMPTY, "B"))),
                                                        Optional.empty()))))));

        var expectedNode = new NamespaceNode<>(
                Meta.of(namespaceName), idNode,
                List.of(),
                List.of(
                        // `A` and `B` end up in the same declaration group,
                        // sorted in their original declaration ordering
                        List.of(
                                dataNode(new Meta<>(dataBRange, dataBName), "B",
                                        List.of(),
                                        List.of(
                                                constructorNode(
                                                        Meta.of(mkBName), "MkB",
                                                        List.of(
                                                                constructorParamNode(Meta.of(mkBFieldName), "a",
                                                                        typeRefNode(Meta.of(dataAName), "A"))),
                                                        Optional.empty()))),
                                dataNode(new Meta<>(dataARange, dataAName), "A",
                                        List.of(),
                                        List.of(
                                                constructorNode(
                                                        Meta.of(mkAName), "MkA",
                                                        List.of(
                                                                constructorParamNode(Meta.of(mkAFieldName), "b",
                                                                        typeRefNode(Meta.of(dataBName), "B"))),
                                                        Optional.empty())))),
                        // `C` is in a later group as it depends on `B`
                        List.of(
                                dataNode(
                                        new Meta<>(dataCRange, dataCName), "C",
                                        List.of(),
                                        List.of(
                                                constructorNode(
                                                        Meta.of(mkCName), "MkC",
                                                        List.of(
                                                                constructorParamNode(Meta.of(mkCFieldName), "b",
                                                                        typeRefNode(Meta.of(dataBName), "B"))),
                                                        Optional.empty()))))));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameSimpleDataDuplicateConstructor() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var originalConstructorRange = new Range(2, 4, 2, 14);
        var duplicateConstructorRange = new Range(4, 4, 4, 14);

        /*-
         * namespace Mina/Test/Renamer {
         *   data Boolean {
         *     case True()
         *     case False()
         *     case True()
         *   }
         * }
         */
        var originalNode = namespaceNode(
                Range.EMPTY, idNode, List.of(),
                List.of(
                        dataNode(Range.EMPTY, "Boolean", List.of(), List.of(
                                constructorNode(originalConstructorRange, "True", List.of(),
                                        Optional.empty()),
                                constructorNode(Range.EMPTY, "False", List.of(),
                                        Optional.empty()),
                                constructorNode(duplicateConstructorRange, "True", List.of(),
                                        Optional.empty())))));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        var diagnostics = collector.getDiagnostics();

        assertThat(diagnostics, hasSize(1));

        assertDuplicateValueDefinition(
                diagnostics,
                duplicateConstructorRange,
                originalConstructorRange,
                "True");
    }

    @Disabled("At present constructors do not introduce a new type")
    @Test
    void renameDataCollidingWithConstructor() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var originalDataRange = new Range(1, 7, 1, 13);
        var collidingConstructorRange = new Range(6, 9, 6, 15);

        /*-
         * namespace Mina/Test/Renamer {
         *   data Boolean {
         *     case True()
         *     case False()
         *   }
         *   data ValueType {
         *     case Boolean()
         *     case Number()
         *   }
         * }
         */
        var originalNode = namespaceNode(
                Range.EMPTY, idNode, List.of(),
                List.of(
                        dataNode(originalDataRange, "Boolean", List.of(), List.of(
                                constructorNode(Range.EMPTY, "True", List.of(),
                                        Optional.empty()),
                                constructorNode(Range.EMPTY, "False", List.of(),
                                        Optional.empty()))),
                        dataNode(Range.EMPTY, "ValueType", List.of(), List.of(
                                constructorNode(collidingConstructorRange, "Boolean", List.of(),
                                        Optional.empty()),
                                constructorNode(Range.EMPTY, "Number", List.of(),
                                        Optional.empty())))));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDuplicateTypeDefinition(
                collector.getDiagnostics(),
                collidingConstructorRange, originalDataRange, "Boolean");
    }

    @Test
    void renameListDataType() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var namespaceName = new NamespaceName(List.of("Mina", "Test"), "Renamer");
        var dataName = new DataName(new QualifiedName(namespaceName, "List"));
        var tyVarName = new ForAllVarName("A");
        var consName = new ConstructorName(dataName, new QualifiedName(namespaceName, "Cons"));
        var headName = new FieldName(consName, "head");
        var tailName = new FieldName(consName, "tail");
        var nilName = new ConstructorName(dataName, new QualifiedName(namespaceName, "Nil"));

        var originalConsNode = constructorNode(
                Range.EMPTY, "Cons", List.of(
                        constructorParamNode(
                                Range.EMPTY, "head", typeRefNode(Range.EMPTY, "A")),
                        constructorParamNode(
                                Range.EMPTY, "tail",
                                typeApplyNode(Range.EMPTY,
                                        typeRefNode(Range.EMPTY, "List"),
                                        List.of(typeRefNode(Range.EMPTY, "A"))))),
                Optional.empty());

        var originalNilNode = constructorNode(Range.EMPTY, "Nil", List.of(), Optional.empty());

        /*-
         * namespace Mina/Test/Renamer {
         *   data List[A] {
         *     case Cons(head: A, tail: List[A])
         *     case Nil()
         *   }
         * }
         */
        var originalNode = namespaceNode(
                Range.EMPTY,
                idNode,
                List.of(),
                List.of(
                        dataNode(Range.EMPTY, "List",
                                List.of(forAllVarNode(Range.EMPTY, "A")),
                                List.of(originalConsNode, originalNilNode))));

        var expectedConsNode = constructorNode(
                Meta.<Name>of(consName),
                "Cons",
                List.of(
                        constructorParamNode(
                                Meta.<Name>of(headName),
                                "head",
                                typeRefNode(Meta.<Name>of(tyVarName), "A")),
                        constructorParamNode(
                                Meta.of(tailName), "tail",
                                typeApplyNode(
                                        Meta.of(Nameless.INSTANCE),
                                        typeRefNode(Meta.of(dataName), "List"),
                                        List.of(
                                                typeRefNode(Meta.of(tyVarName), "A"))))),
                Optional.empty());

        var expectedNilNode = constructorNode(
                Meta.<Name>of(nilName),
                "Nil", List.of(), Optional.empty());

        var expectedNode = namespaceNode(
                Meta.<Name>of(namespaceName), idNode, List.of(),
                List.of(
                        dataNode(Meta.<Name>of(dataName), "List",
                                List.of(forAllVarNode(Meta.<Name>of(tyVarName), "A")),
                                List.of(expectedConsNode, expectedNilNode))));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameListDataTypeUnknownTypeParam() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var unknownTyParamRange = new Range(2, 20, 2, 21);

        var originalConsNode = constructorNode(
                Range.EMPTY, "Cons", List.of(
                        constructorParamNode(
                                Range.EMPTY, "head", typeRefNode(unknownTyParamRange, "B")),
                        constructorParamNode(
                                Range.EMPTY, "tail",
                                typeApplyNode(Range.EMPTY,
                                        typeRefNode(Range.EMPTY, "List"),
                                        List.of(typeRefNode(Range.EMPTY, "A"))))),
                Optional.empty());

        var originalNilNode = constructorNode(Range.EMPTY, "Nil", List.of(), Optional.empty());

        /*-
         * namespace Mina/Test/Renamer {
         *   data List[A] {
         *     case Cons(head: B, tail: List[A])
         *     case Nil()
         *   }
         * }
         */
        var originalNode = namespaceNode(
                Range.EMPTY,
                idNode,
                List.of(),
                List.of(
                        dataNode(Range.EMPTY, "List",
                                List.of(forAllVarNode(Range.EMPTY, "A")),
                                List.of(originalConsNode, originalNilNode))));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                unknownTyParamRange,
                "Reference to undefined type 'B'");
    }

    @Test
    void renameListDataTypeDuplicateTypeParam() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var originalConsNode = constructorNode(
                Range.EMPTY, "Cons", List.of(
                        constructorParamNode(
                                Range.EMPTY, "head", typeRefNode(Range.EMPTY, "A")),
                        constructorParamNode(
                                Range.EMPTY, "tail",
                                typeApplyNode(Range.EMPTY,
                                        typeRefNode(Range.EMPTY, "List"),
                                        List.of(typeRefNode(Range.EMPTY, "A"))))),
                Optional.empty());

        var originalNilNode = constructorNode(Range.EMPTY, "Nil", List.of(), Optional.empty());

        var originalTyParamRange = new Range(1, 12, 1, 13);
        var duplicateTyParamRange = new Range(1, 15, 1, 16);

        /*-
         * namespace Mina/Test/Renamer {
         *   data List[A, A] {
         *     case Cons(head: A, tail: List[A])
         *     case Nil()
         *   }
         * }
         */
        var originalNode = namespaceNode(
                Range.EMPTY,
                idNode,
                List.of(),
                List.of(
                        dataNode(Range.EMPTY, "List",
                                List.of(
                                        forAllVarNode(originalTyParamRange, "A"),
                                        forAllVarNode(duplicateTyParamRange, "A")),
                                List.of(originalConsNode, originalNilNode))));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDuplicateTypeDefinition(
                collector.getDiagnostics(),
                duplicateTyParamRange, originalTyParamRange, "A");
    }

    @Test
    void renameListDataTypeDuplicateConstrParam() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var originalParamRange = new Range(2, 14, 2, 21);
        var duplicateParamRange = new Range(2, 23, 2, 30);

        var originalConsNode = constructorNode(
                Range.EMPTY, "Cons", List.of(
                        constructorParamNode(
                                originalParamRange, "head", typeRefNode(Range.EMPTY, "A")),
                        constructorParamNode(
                                duplicateParamRange, "head", typeRefNode(Range.EMPTY, "A")),
                        constructorParamNode(
                                Range.EMPTY, "tail",
                                typeApplyNode(Range.EMPTY,
                                        typeRefNode(Range.EMPTY, "List"),
                                        List.of(typeRefNode(Range.EMPTY, "A"))))),
                Optional.empty());

        var originalNilNode = constructorNode(Range.EMPTY, "Nil", List.of(), Optional.empty());

        /*-
         * namespace Mina/Test/Renamer {
         *   data List[A] {
         *     case Cons(head: A, head: A, tail: List[A])
         *     case Nil()
         *   }
         * }
         */
        var originalNode = namespaceNode(
                Range.EMPTY,
                idNode,
                List.of(),
                List.of(
                        dataNode(Range.EMPTY, "List",
                                List.of(forAllVarNode(Range.EMPTY, "A")),
                                List.of(originalConsNode, originalNilNode))));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDuplicateFieldDefinition(
                collector.getDiagnostics(),
                duplicateParamRange, originalParamRange, "head", "Cons");
    }

    @Test
    void renameLetDeclaration() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var namespaceName = new NamespaceName(List.of("Mina", "Test"), "Renamer");
        var letName = new LetName(new QualifiedName(namespaceName, "one"));

        /*-
         * namespace Mina/Test/Renamer {
         *   let one = 1
         * }
         */
        var originalNode = namespaceNode(Range.EMPTY, idNode, List.of(),
                List.of(letNode(Range.EMPTY, "one", intNode(Range.EMPTY, 1))));

        var expectedNode = namespaceNode(Meta.of(namespaceName), idNode, List.of(),
                List.of(letNode(Meta.of(letName), "one", intNode(Meta.of(Nameless.INSTANCE), 1))));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLetDuplicateDefinition() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var originalLetRange = new Range(1, 2, 1, 12);
        var duplicateLetRange = new Range(2, 2, 2, 12);

        /*-
         * namespace Mina/Test/Renamer {
         *   let one = 1
         *   let one = 2
         * }
         */
        var originalNode = namespaceNode(Range.EMPTY, idNode, List.of(),
                List.of(
                        letNode(originalLetRange, "one", intNode(Range.EMPTY, 1)),
                        letNode(duplicateLetRange, "one", intNode(Range.EMPTY, 2))));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDuplicateValueDefinition(
                collector.getDiagnostics(),
                duplicateLetRange, originalLetRange, "one");
    }

    @Test
    void renameLetWithType() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var namespaceName = new NamespaceName(List.of("Mina", "Test"), "Renamer");
        var letName = new LetName(new QualifiedName(namespaceName, "id"));
        var typeVarAName = new ForAllVarName("A");
        var paramAName = new LocalName("a", 0);

        /*-
         * namespace Mina/Test/Renamer {
         *   let id: [A] { A -> A } = a -> a
         * }
         */
        var originalNode = namespaceNode(Range.EMPTY, idNode, List.of(),
                List.of(
                        letNode(Range.EMPTY, "id",
                                quantifiedTypeNode(
                                        Range.EMPTY,
                                        List.of(forAllVarNode(Range.EMPTY, "A")),
                                        funTypeNode(Range.EMPTY,
                                                List.of(typeRefNode(Range.EMPTY, "A")),
                                                typeRefNode(Range.EMPTY, "A"))),
                                lambdaNode(
                                        Range.EMPTY,
                                        List.of(paramNode(Range.EMPTY, "a")),
                                        refNode(Range.EMPTY, "a")))));

        var expectedNode = namespaceNode(Meta.of(namespaceName), idNode, List.of(),
                List.of(
                        letNode(Meta.of(letName), "id",
                                quantifiedTypeNode(
                                        Meta.of(Nameless.INSTANCE),
                                        List.of(forAllVarNode(Meta.of(typeVarAName), "A")),
                                        funTypeNode(Meta.of(Nameless.INSTANCE),
                                                List.of(typeRefNode(Meta.of(typeVarAName), "A")),
                                                typeRefNode(Meta.of(typeVarAName), "A"))),
                                lambdaNode(
                                        Meta.of(Nameless.INSTANCE),
                                        List.of(paramNode(Meta.of(paramAName), "a")),
                                        refNode(Meta.of(paramAName), "a")))));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLetWithReference() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var namespaceName = new NamespaceName(List.of("Mina", "Test"), "Renamer");
        var fooName = new LetName(new QualifiedName(namespaceName, "foo"));
        var barName = new LetName(new QualifiedName(namespaceName, "bar"));

        /*-
         * namespace Mina/Test/Renamer {
         *   let bar = 1
         *   let foo = bar
         * }
         */
        var originalNode = namespaceNode(Range.EMPTY, idNode, List.of(),
                List.of(
                        letNode(Range.EMPTY, "bar", intNode(Range.EMPTY, 1)),
                        letNode(Range.EMPTY, "foo", refNode(Range.EMPTY, "bar"))));

        var expectedNode = new NamespaceNode<>(Meta.of(namespaceName), idNode, List.of(),
                List.of(
                        List.of(letNode(Meta.of(barName), "bar", intNode(Meta.of(Nameless.INSTANCE), 1))),
                        List.of(letNode(Meta.of(fooName), "foo", refNode(Meta.of(barName), "bar")))));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLetInvalidReference() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var unknownReferenceRange = new Range(2, 12, 2, 15);

        /*-
         * namespace Mina/Test/Renamer {
         *   let bar = 1
         *   let foo = baz
         * }
         */
        var originalNode = namespaceNode(Range.EMPTY, idNode, List.of(),
                List.of(
                        letNode(Range.EMPTY, "bar", intNode(Range.EMPTY, 1)),
                        letNode(Range.EMPTY, "foo", refNode(unknownReferenceRange, "baz"))));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                unknownReferenceRange,
                "Reference to undefined value 'baz'");
    }

    @Test
    void renameLetFnDeclaration() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var namespaceName = new NamespaceName(List.of("Mina", "Test"), "Renamer");
        var letName = new LetName(new QualifiedName(namespaceName, "const"));
        var paramAName = new LocalName("a", 0);
        var paramBName = new LocalName("b", 1);
        var typeVarAName = new ForAllVarName("A");
        var typeVarBName = new ForAllVarName("B");

        /*-
         * namespace Mina/Test/Renamer {
         *   let const[A, B](a: A, b: B): A = a
         * }
         */
        var originalNode = namespaceNode(Range.EMPTY, idNode, List.of(),
                List.of(
                        letFnNode(Range.EMPTY, "const",
                                List.of(
                                        forAllVarNode(Range.EMPTY, "A"),
                                        forAllVarNode(Range.EMPTY, "B")),
                                List.of(
                                        paramNode(Range.EMPTY, "a", typeRefNode(Range.EMPTY, "A")),
                                        paramNode(Range.EMPTY, "b", typeRefNode(Range.EMPTY, "B"))),
                                typeRefNode(Range.EMPTY, "A"),
                                refNode(Range.EMPTY, "a"))));

        var expectedNode = namespaceNode(Meta.of(namespaceName), idNode, List.of(),
                List.of(
                        letFnNode(Meta.of(letName), "const",
                                List.of(
                                        forAllVarNode(Meta.of(typeVarAName), "A"),
                                        forAllVarNode(Meta.of(typeVarBName), "B")),
                                List.of(
                                        paramNode(Meta.of(paramAName), "a", typeRefNode(Meta.of(typeVarAName), "A")),
                                        paramNode(Meta.of(paramBName), "b", typeRefNode(Meta.of(typeVarBName), "B"))),
                                typeRefNode(Meta.of(typeVarAName), "A"),
                                refNode(Meta.of(paramAName), "a"))));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLetFnUnknownTypeVar() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var unknownTypeVarRange = new Range(1, 24, 1, 25);

        /*-
         * namespace Mina/Test/Renamer {
         *   let const[A](a: A, b: B): A = a
         * }
         */
        var originalNode = namespaceNode(Range.EMPTY, idNode, List.of(),
                List.of(
                        letFnNode(Range.EMPTY, "const",
                                List.of(forAllVarNode(Range.EMPTY, "A")),
                                List.of(
                                        paramNode(Range.EMPTY, "a", typeRefNode(Range.EMPTY, "A")),
                                        paramNode(Range.EMPTY, "b", typeRefNode(unknownTypeVarRange, "B"))),
                                typeRefNode(Range.EMPTY, "A"),
                                refNode(Range.EMPTY, "a"))));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                unknownTypeVarRange,
                "Reference to undefined type 'B'");
    }

    @Test
    void renameLetFnDuplicateTypeVar() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var originalTypeVarRange = new Range(1, 12, 1, 13);
        var duplicateTypeVarRange = new Range(1, 15, 1, 16);

        /*-
         * namespace Mina/Test/Renamer {
         *   let const[A, A](a: A, b: A): A = a
         * }
         */
        var originalNode = namespaceNode(Range.EMPTY, idNode, List.of(),
                List.of(
                        letFnNode(Range.EMPTY, "const",
                                List.of(
                                        forAllVarNode(originalTypeVarRange, "A"),
                                        forAllVarNode(duplicateTypeVarRange, "A")),
                                List.of(
                                        paramNode(Range.EMPTY, "a", typeRefNode(Range.EMPTY, "A")),
                                        paramNode(Range.EMPTY, "b", typeRefNode(Range.EMPTY, "A"))),
                                typeRefNode(Range.EMPTY, "A"),
                                refNode(Range.EMPTY, "a"))));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDuplicateTypeDefinition(
                collector.getDiagnostics(),
                duplicateTypeVarRange, originalTypeVarRange, "A");
    }

    @Test
    void renameLetFnUnknownParam() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var unknownParamRange = new Range(1, 35, 1, 36);

        /*-
         * namespace Mina/Test/Renamer {
         *   let const[A, B](a: A, b: B): A = x
         * }
         */
        var originalNode = namespaceNode(Range.EMPTY, idNode, List.of(),
                List.of(
                        letFnNode(Range.EMPTY, "const",
                                List.of(
                                        forAllVarNode(Range.EMPTY, "A"),
                                        forAllVarNode(Range.EMPTY, "B")),
                                List.of(
                                        paramNode(Range.EMPTY, "a", typeRefNode(Range.EMPTY, "A")),
                                        paramNode(Range.EMPTY, "b", typeRefNode(Range.EMPTY, "B"))),
                                typeRefNode(Range.EMPTY, "A"),
                                refNode(unknownParamRange, "x"))));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                unknownParamRange,
                "Reference to undefined value 'x'");
    }

    @Test
    void renameLetFnDuplicateParam() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var originalParamRange = new Range(1, 18, 1, 19);
        var duplicateParamRange = new Range(1, 24, 1, 25);

        /*-
         * namespace Mina/Test/Renamer {
         *   let const[A, B](a: A, a: B): A = a
         * }
         */
        var originalNode = namespaceNode(Range.EMPTY, idNode, List.of(),
                List.of(
                        letFnNode(Range.EMPTY, "const",
                                List.of(
                                        forAllVarNode(Range.EMPTY, "A"),
                                        forAllVarNode(Range.EMPTY, "B")),
                                List.of(
                                        paramNode(originalParamRange, "a", typeRefNode(Range.EMPTY, "A")),
                                        paramNode(duplicateParamRange, "a", typeRefNode(Range.EMPTY, "B"))),
                                typeRefNode(Range.EMPTY, "A"),
                                refNode(Range.EMPTY, "a"))));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDuplicateValueDefinition(
                collector.getDiagnostics(),
                duplicateParamRange, originalParamRange, "a");
    }

    @Test
    void renameLetMutualRecursion() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var namespaceName = new NamespaceName(List.of("Mina", "Test"), "Renamer");

        var letAName = new LetName(new QualifiedName(namespaceName, "a"));
        var letBName = new LetName(new QualifiedName(namespaceName, "b"));
        var letCName = new LetName(new QualifiedName(namespaceName, "c"));

        var letCRange = new Range(1, 2, 1, 10);
        var letARange = new Range(2, 2, 2, 16);
        var letBRange = new Range(3, 2, 3, 16);

        /*-
         * namespace Mina/Test/Renamer {
         *   let c = b
         *   let a = () -> b
         *   let b = () -> a
         * }
         */
        var originalNode = new NamespaceNode<>(Meta.of(Range.EMPTY), idNode, List.of(),
                List.of(
                        List.of(
                                letNode(letCRange, "c",
                                        refNode(Range.EMPTY, "b")),
                                letNode(letARange, "a",
                                        lambdaNode(Range.EMPTY, List.of(), refNode(Range.EMPTY, "b"))),
                                letNode(letBRange, "b",
                                        lambdaNode(Range.EMPTY, List.of(), refNode(Range.EMPTY, "a"))))));

        var expectedNode = new NamespaceNode<>(
                Meta.of(namespaceName), idNode,
                List.of(),
                List.of(
                        // `a` and `b` end up in the same declaration group,
                        // sorted in their original declaration ordering
                        List.of(
                                letNode(
                                        new Meta<>(letARange, letAName), "a",
                                        lambdaNode(
                                                Meta.of(Nameless.INSTANCE),
                                                List.of(),
                                                refNode(Meta.of(letBName), "b"))),
                                letNode(
                                        new Meta<>(letBRange, letBName), "b",
                                        lambdaNode(
                                                Meta.of(Nameless.INSTANCE),
                                                List.of(),
                                                refNode(Meta.of(letAName), "a")))),
                        // `c` is in a later group as it depends on `b`
                        List.of(
                                letNode(
                                        new Meta<>(letCRange, letCName), "c",
                                        refNode(Meta.of(letBName), "b")))));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    // Types
    @Test
    void renameQuantifiedType() {
        var typeVarAName = new ForAllVarName("A");
        var typeVarBName = new ForAllVarName("B");

        /*- [A, B] { A } */
        var originalNode = quantifiedTypeNode(
                Range.EMPTY,
                List.of(
                        forAllVarNode(Range.EMPTY, "A"),
                        forAllVarNode(Range.EMPTY, "B")),
                typeRefNode(Range.EMPTY, "A"));

        var expectedNode = quantifiedTypeNode(
                Meta.of(Nameless.INSTANCE),
                List.of(
                        forAllVarNode(Meta.of(typeVarAName), "A"),
                        forAllVarNode(Meta.of(typeVarBName), "B")),
                typeRefNode(Meta.of(typeVarAName), "A"));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameQuantifiedTypeWithExistsVar() {
        var typeVarAName = new ExistsVarName("?A");
        var typeVarBName = new ExistsVarName("?B");

        /*- [?A, ?B] { ?A } */
        var originalNode = quantifiedTypeNode(
                Range.EMPTY,
                List.of(
                        existsVarNode(Range.EMPTY, "?A"),
                        existsVarNode(Range.EMPTY, "?B")),
                typeRefNode(Range.EMPTY, "?A"));

        var expectedNode = quantifiedTypeNode(
                Meta.of(Nameless.INSTANCE),
                List.of(
                        existsVarNode(Meta.of(typeVarAName), "?A"),
                        existsVarNode(Meta.of(typeVarBName), "?B")),
                typeRefNode(Meta.of(typeVarAName), "?A"));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameQuantifiedTypeWithDuplicateTypeVar() {
        var originalTypeVarRange = new Range(0, 1, 0, 2);
        var duplicateTypeVarRange = new Range(0, 4, 0, 5);

        /*- [A, A] { A } */
        var originalNode = quantifiedTypeNode(
                Range.EMPTY,
                List.of(
                        forAllVarNode(originalTypeVarRange, "A"),
                        forAllVarNode(duplicateTypeVarRange, "A")),
                typeRefNode(Range.EMPTY, "A"));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDuplicateTypeDefinition(
                collector.getDiagnostics(),
                duplicateTypeVarRange, originalTypeVarRange, "A");
    }

    @Test
    void renameFunType() {
        var typeVarAName = new ForAllVarName("A");
        var typeVarBName = new ForAllVarName("B");

        /*- [A, B] { (A, B) -> A } */
        var originalNode = quantifiedTypeNode(
                Range.EMPTY,
                List.of(
                        forAllVarNode(Range.EMPTY, "A"),
                        forAllVarNode(Range.EMPTY, "B")),
                funTypeNode(
                        Range.EMPTY,
                        List.of(
                                typeRefNode(Range.EMPTY, "A"),
                                typeRefNode(Range.EMPTY, "B")),
                        typeRefNode(Range.EMPTY, "A")));

        var expectedNode = quantifiedTypeNode(
                Meta.of(Nameless.INSTANCE),
                List.of(
                        forAllVarNode(Meta.of(typeVarAName), "A"),
                        forAllVarNode(Meta.of(typeVarBName), "B")),
                funTypeNode(
                        Meta.of(Nameless.INSTANCE),
                        List.of(
                                typeRefNode(Meta.of(typeVarAName), "A"),
                                typeRefNode(Meta.of(typeVarBName), "B")),
                        typeRefNode(Meta.of(typeVarAName), "A")));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameFunTypeUnknownTypeVar() {
        var unknownTypeVarRange = new Range(0, 17, 0, 18);

        /*- [A] { (A, B) -> A } */
        var originalNode = quantifiedTypeNode(
                Range.EMPTY,
                List.of(forAllVarNode(Range.EMPTY, "A")),
                funTypeNode(
                        Range.EMPTY,
                        List.of(
                                typeRefNode(Range.EMPTY, "A"),
                                typeRefNode(unknownTypeVarRange, "B")),
                        typeRefNode(Range.EMPTY, "A")));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                unknownTypeVarRange,
                "Reference to undefined type 'B'");
    }

    @Test
    void renameTypeApply() {
        var typeVarFName = new ForAllVarName("F");
        var typeVarAName = new ForAllVarName("A");

        /*- [F, A] { F[A] } */
        var originalNode = quantifiedTypeNode(
                Range.EMPTY,
                List.of(
                        forAllVarNode(Range.EMPTY, "F"),
                        forAllVarNode(Range.EMPTY, "A")),
                typeApplyNode(
                        Range.EMPTY,
                        typeRefNode(Range.EMPTY, "F"),
                        List.of(typeRefNode(Range.EMPTY, "A"))));

        var expectedNode = quantifiedTypeNode(
                Meta.of(Nameless.INSTANCE),
                List.of(
                        forAllVarNode(Meta.of(typeVarFName), "F"),
                        forAllVarNode(Meta.of(typeVarAName), "A")),
                typeApplyNode(
                        Meta.of(Nameless.INSTANCE),
                        typeRefNode(Meta.of(typeVarFName), "F"),
                        List.of(typeRefNode(Meta.of(typeVarAName), "A"))));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameTypeApplyUnknownTypeVar() {
        var unknownTypeVarRange = new Range(0, 9, 0, 10);

        /*- [F] { F[A] } */
        var originalNode = quantifiedTypeNode(
                Range.EMPTY,
                List.of(forAllVarNode(Range.EMPTY, "F")),
                typeApplyNode(
                        Range.EMPTY,
                        typeRefNode(Range.EMPTY, "F"),
                        List.of(typeRefNode(unknownTypeVarRange, "A"))));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                unknownTypeVarRange,
                "Reference to undefined type 'A'");
    }

    // Expressions
    @Test
    void renameLambda() {
        var paramName = new LocalName("a", 0);

        /* a -> a */
        var originalNode = lambdaNode(
                Range.EMPTY,
                List.of(paramNode(Range.EMPTY, "a")),
                refNode(Range.EMPTY, "a"));

        var expectedNode = lambdaNode(
                Meta.of(Nameless.INSTANCE),
                List.of(
                        paramNode(Meta.of(paramName), "a")),
                refNode(Meta.of(paramName), "a"));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLambdaInnerNameShadowsOuter() {
        var outerParamName = new LocalName("a", 0);
        var innerParamName = new LocalName("a", 1);

        var outerParamRange = new Range(0, 0, 0, 1);
        var innerParamRange = new Range(0, 5, 0, 6);

        /* a -> a -> a */
        var originalNode = lambdaNode(
                Range.EMPTY,
                List.of(paramNode(outerParamRange, "a")),
                lambdaNode(
                        Range.EMPTY,
                        List.of(paramNode(innerParamRange, "a")),
                        refNode(Range.EMPTY, "a")));

        var expectedNode = lambdaNode(
                Meta.of(Nameless.INSTANCE),
                List.of(
                        paramNode(new Meta<>(outerParamRange, outerParamName), "a")),
                lambdaNode(
                        Meta.of(Nameless.INSTANCE),
                        List.of(paramNode(new Meta<>(innerParamRange, innerParamName), "a")),
                        refNode(Meta.of(innerParamName), "a")));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLambdaInvalidReference() {
        var unknownVariableRange = new Range(0, 5, 0, 6);

        /* a -> x */
        var originalNode = lambdaNode(
                Range.EMPTY,
                List.of(paramNode(Range.EMPTY, "a")),
                refNode(unknownVariableRange, "x"));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                unknownVariableRange,
                "Reference to undefined value 'x'");
    }

    @Test
    void renameBlock() {
        var localLetName = new LocalName("bar", 0);

        /*-
         * {
         *   let bar = 1
         *   bar
         * }
         */
        var originalNode = blockNode(
                Range.EMPTY,
                List.of(
                        letNode(Range.EMPTY, "bar", intNode(Range.EMPTY, 1))),
                refNode(Range.EMPTY, "bar"));

        var expectedNode = blockNode(
                Meta.of(Nameless.INSTANCE),
                List.of(
                        letNode(Meta.of(localLetName), "bar", intNode(Meta.of(Nameless.INSTANCE), 1))),
                refNode(Meta.of(localLetName), "bar"));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameBlockDuplicateLet() {
        var originalLetRange = new Range(0, 1, 0, 2);
        var duplicateLetRange = new Range(0, 4, 0, 5);

        /*-
         * {
         *   let bar = 1
         *   let bar = 1
         *   bar
         * }
         */
        var originalNode = blockNode(
                Range.EMPTY,
                List.of(
                        letNode(originalLetRange, "bar", intNode(Range.EMPTY, 1)),
                        letNode(duplicateLetRange, "bar", intNode(Range.EMPTY, 1))),
                refNode(Range.EMPTY, "bar"));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDuplicateValueDefinition(
                collector.getDiagnostics(),
                duplicateLetRange, originalLetRange, "bar");
    }

    @Test
    void renameBlockNoForwardReference() {
        var invalidRefRange = new Range(0, 12, 0, 14);

        /*-
         * {
         *   let foo = bar
         *   let bar = 1
         *   foo
         * }
         */
        var originalNode = blockNode(
                Range.EMPTY,
                List.of(
                        letNode(Range.EMPTY, "foo", refNode(invalidRefRange, "bar")),
                        letNode(invalidRefRange, "bar", intNode(Range.EMPTY, 1))),
                refNode(Range.EMPTY, "bar"));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        // TODO: Specific error message for forward references
        assertDiagnostic(
                collector.getDiagnostics(),
                invalidRefRange,
                "Reference to undefined value 'bar'");
    }

    @Test
    void renameBlockLocalLetShadowsOuter() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var namespaceName = new NamespaceName(List.of("Mina", "Test"), "Renamer");
        var outerFooName = new LetName(new QualifiedName(namespaceName, "foo"));
        var outerBarName = new LetName(new QualifiedName(namespaceName, "bar"));
        var localBarName = new LocalName("bar", 0);

        /*-
         * namespace Mina/Test/Renamer {
         *   let bar = 1
         *   let foo = {
         *     let bar = 2
         *     bar
         *   }
         * }
         */
        var originalNode = namespaceNode(
                Range.EMPTY, idNode,
                List.of(),
                List.of(
                        letNode(Range.EMPTY, "bar", intNode(Range.EMPTY, 1)),
                        letNode(Range.EMPTY, "foo", blockNode(
                                Range.EMPTY,
                                List.of(
                                        letNode(Range.EMPTY, "bar", intNode(Range.EMPTY, 1))),
                                refNode(Range.EMPTY, "bar")))));

        var expectedNode = namespaceNode(
                Meta.of(namespaceName), idNode,
                List.of(),
                List.of(
                        letNode(Meta.of(outerBarName), "bar", intNode(Meta.of(Nameless.INSTANCE), 1)),
                        letNode(Meta.of(outerFooName), "foo", blockNode(
                                Meta.of(Nameless.INSTANCE),
                                List.of(
                                        letNode(Meta.of(localBarName), "bar", intNode(Meta.of(Nameless.INSTANCE), 1))),
                                refNode(Meta.of(localBarName), "bar")))));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameBlockLocalLetReferencesOuter() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var namespaceName = new NamespaceName(List.of("Mina", "Test"), "Renamer");
        var outerFooName = new LetName(new QualifiedName(namespaceName, "foo"));
        var outerBarName = new LetName(new QualifiedName(namespaceName, "bar"));
        var localBazName = new LocalName("baz", 0);

        /*-
         * namespace Mina/Test/Renamer {
         *   let bar = 1
         *   let foo = {
         *     let baz = bar
         *     baz
         *   }
         * }
         */
        var originalNode = namespaceNode(
                Range.EMPTY, idNode,
                List.of(),
                List.of(
                        letNode(Range.EMPTY, "bar", intNode(Range.EMPTY, 1)),
                        letNode(Range.EMPTY, "foo", blockNode(
                                Range.EMPTY,
                                List.of(
                                        letNode(Range.EMPTY, "baz", refNode(Range.EMPTY, "bar"))),
                                refNode(Range.EMPTY, "baz")))));

        var expectedNode = new NamespaceNode<>(
                Meta.of(namespaceName), idNode,
                List.of(),
                List.of(
                        List.of(letNode(Meta.of(outerBarName), "bar", intNode(Meta.of(Nameless.INSTANCE), 1))),
                        List.of(letNode(Meta.of(outerFooName), "foo", blockNode(
                                Meta.of(Nameless.INSTANCE),
                                List.of(
                                        letNode(Meta.of(localBazName), "baz", refNode(Meta.of(outerBarName), "bar"))),
                                refNode(Meta.of(localBazName), "baz"))))));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameBlockLocalLetInvisibleToOuter() {
        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var invalidRefRange = new Range(5, 12, 5, 15);

        /*-
         * namespace Mina/Test/Renamer {
         *   let foo = {
         *     let bar = 2
         *     bar
         *   }
         *   let baz = bar
         * }
         */
        var originalNode = namespaceNode(
                Range.EMPTY, idNode,
                List.of(),
                List.of(
                        letNode(Range.EMPTY, "foo", blockNode(
                                Range.EMPTY,
                                List.of(
                                        letNode(Range.EMPTY, "bar", intNode(Range.EMPTY, 1))),
                                refNode(Range.EMPTY, "bar"))),
                        letNode(Range.EMPTY, "baz", refNode(invalidRefRange, "bar"))));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                invalidRefRange,
                "Reference to undefined value 'bar'");
    }

    @Test
    void renameIf() {
        /* if true then 1 else 2 */
        var originalNode = ifNode(
                Range.EMPTY,
                boolNode(Range.EMPTY, true),
                intNode(Range.EMPTY, 1),
                intNode(Range.EMPTY, 2));

        var expectedNode = ifNode(
                Meta.of(Nameless.INSTANCE),
                boolNode(Meta.of(Nameless.INSTANCE), true),
                intNode(Meta.of(Nameless.INSTANCE), 1),
                intNode(Meta.of(Nameless.INSTANCE), 2));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameIfBranchesHaveIndependentScope() {

        var firstLocalFooName = new LocalName("foo", 0);
        var secondLocalFooName = new LocalName("foo", 1);

        /*-
         * if true then {
         *   let foo = 1
         *   foo
         * } else {
         *   let foo = 2
         *   foo
         * }
        */
        var originalNode = ifNode(
                Range.EMPTY,
                boolNode(Range.EMPTY, true),
                blockNode(
                        Range.EMPTY,
                        List.of(letNode(Range.EMPTY, "foo", intNode(Range.EMPTY, 1))),
                        refNode(Range.EMPTY, "foo")),
                blockNode(
                        Range.EMPTY,
                        List.of(letNode(Range.EMPTY, "foo", intNode(Range.EMPTY, 2))),
                        refNode(Range.EMPTY, "foo")));

        var expectedNode = ifNode(
                Meta.of(Nameless.INSTANCE),
                boolNode(Meta.of(Nameless.INSTANCE), true),
                blockNode(
                        Meta.of(Nameless.INSTANCE),
                        List.of(letNode(Meta.of(firstLocalFooName), "foo", intNode(Meta.of(Nameless.INSTANCE), 1))),
                        refNode(Meta.of(firstLocalFooName), "foo")),
                blockNode(
                        Meta.of(Nameless.INSTANCE),
                        List.of(letNode(Meta.of(secondLocalFooName), "foo",
                                        intNode(Meta.of(Nameless.INSTANCE), 2))),
                        refNode(Meta.of(secondLocalFooName), "foo")));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameIfInvalidReference() {
        var invalidRefRange = new Range(4, 2, 4, 4);

        /*-
         * if true then {
         *   let foo = 1
         *   foo
         * } else {
         *   foo
         * }
        */
        var originalNode = ifNode(
                Range.EMPTY,
                boolNode(Range.EMPTY, true),
                blockNode(
                        Range.EMPTY,
                        List.of(letNode(Range.EMPTY, "foo", intNode(Range.EMPTY, 1))),
                        refNode(Range.EMPTY, "foo")),
                blockNode(
                        Range.EMPTY,
                        refNode(invalidRefRange, "foo")));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                invalidRefRange,
                "Reference to undefined value 'foo'");
    }

    @Test
    void renameApply() {
        var paramFName = new LocalName("f", 0);
        var paramAName = new LocalName("a", 1);

        /* (f, a) -> f(a) */
        var originalNode = lambdaNode(
                Range.EMPTY,
                List.of(
                        paramNode(Range.EMPTY, "f"),
                        paramNode(Range.EMPTY, "a")),
                applyNode(Range.EMPTY, refNode(Range.EMPTY, "f"), List.of(refNode(Range.EMPTY, "a"))));

        var expectedNode = lambdaNode(
                Meta.of(Nameless.INSTANCE),
                List.of(
                        paramNode(Meta.of(paramFName), "f"),
                        paramNode(Meta.of(paramAName), "a")),
                applyNode(
                        Meta.of(Nameless.INSTANCE),
                        refNode(Meta.of(paramFName), "f"),
                        List.of(refNode(Meta.of(paramAName), "a"))));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameApplyInvalidReference() {
        var invalidRefRange = new Range(0, 5, 0, 6);

        /* a -> f(a) */
        var originalNode = lambdaNode(
                Range.EMPTY,
                List.of(paramNode(Range.EMPTY, "a")),
                applyNode(
                        Range.EMPTY,
                        refNode(invalidRefRange, "f"),
                        List.of(refNode(Range.EMPTY, "a"))));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                invalidRefRange,
                "Reference to undefined value 'f'");
    }

    @Test
    void renameEmptyMatch() {
        var paramAName = new LocalName("a", 0);

        /* a -> match a with {} */
        var originalNode = lambdaNode(
                Range.EMPTY,
                List.of(paramNode(Range.EMPTY, "a")),
                matchNode(
                        Range.EMPTY,
                        refNode(Range.EMPTY, "a"),
                        List.of()));

        var expectedNode = lambdaNode(
                Meta.of(Nameless.INSTANCE),
                List.of(paramNode(Meta.of(paramAName), "a")),
                matchNode(
                        Meta.of(Nameless.INSTANCE),
                        refNode(Meta.of(paramAName), "a"),
                        List.of()));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameMatchCaseLiteralPattern() {
        var paramIntName = new LocalName("int", 0);

        /*-
         * int -> match int with {
         *   case 0 -> true
         *   case 1 -> false
         * }
         */
        var originalNode = lambdaNode(
                Range.EMPTY,
                List.of(paramNode(Range.EMPTY, "int")),
                matchNode(
                        Range.EMPTY,
                        refNode(Range.EMPTY, "int"),
                        List.of(
                                caseNode(
                                        Range.EMPTY,
                                        literalPatternNode(Range.EMPTY, intNode(Range.EMPTY, 0)),
                                        boolNode(Range.EMPTY, false)),
                                caseNode(
                                        Range.EMPTY,
                                        literalPatternNode(Range.EMPTY, intNode(Range.EMPTY, 0)),
                                        boolNode(Range.EMPTY, false)))));

        var expectedNode = lambdaNode(
                Meta.of(Nameless.INSTANCE),
                List.of(paramNode(Meta.of(paramIntName), "int")),
                matchNode(
                        Meta.of(Nameless.INSTANCE),
                        refNode(Meta.of(paramIntName), "int"),
                        List.of(
                                caseNode(
                                        Meta.of(Nameless.INSTANCE),
                                        literalPatternNode(Meta.of(Nameless.INSTANCE),
                                                intNode(Meta.of(Nameless.INSTANCE), 0)),
                                        boolNode(Meta.of(Nameless.INSTANCE), false)),
                                caseNode(
                                        Meta.of(Nameless.INSTANCE),
                                        literalPatternNode(Meta.of(Nameless.INSTANCE),
                                                intNode(Meta.of(Nameless.INSTANCE), 0)),
                                        boolNode(Meta.of(Nameless.INSTANCE), false)))

                ));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameMatchCaseAliasPattern() {
        var outerParamName = new LocalName("a", 0);
        var aliasParamName = new LocalName("a", 1);
        var innerParamName = new LocalName("x", 2);

        /* a -> match a with { case a @ x -> a } */
        var originalNode = lambdaNode(
                Range.EMPTY,
                List.of(paramNode(Range.EMPTY, "a")),
                matchNode(
                        Range.EMPTY,
                        refNode(Range.EMPTY, "a"),
                        List.of(
                                caseNode(
                                        Range.EMPTY,
                                        aliasPatternNode(Range.EMPTY, "a", idPatternNode(Range.EMPTY, "x")),
                                        refNode(Range.EMPTY, "a")))));

        var expectedNode = lambdaNode(
                Meta.of(Nameless.INSTANCE),
                List.of(paramNode(Meta.of(outerParamName), "a")),
                matchNode(
                        Meta.of(Nameless.INSTANCE),
                        refNode(Meta.of(outerParamName), "a"),
                        List.of(
                                caseNode(
                                        Meta.of(Nameless.INSTANCE),
                                        aliasPatternNode(Meta.of(aliasParamName), "a",
                                                idPatternNode(Meta.of(innerParamName), "x")),
                                        refNode(Meta.of(aliasParamName), "a")))));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameMatchIdPatternDuplicatesAlias() {
        var originalVarRange = new Range(0, 25, 0, 26);
        var duplicateVarRange = new Range(0, 29, 0, 30);

        /* a -> match a with { case a @ a -> a } */
        var originalNode = lambdaNode(
                Range.EMPTY,
                List.of(paramNode(Range.EMPTY, "a")),
                matchNode(
                        Range.EMPTY,
                        refNode(Range.EMPTY, "a"),
                        List.of(
                                caseNode(
                                        Range.EMPTY,
                                        aliasPatternNode(originalVarRange, "a", idPatternNode(duplicateVarRange, "a")),
                                        refNode(Range.EMPTY, "a")))));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDuplicateValueDefinition(
                collector.getDiagnostics(),
                duplicateVarRange,
                originalVarRange, "a");
    }

    @Test
    void renameMatchCasesShadowOuterNames() {
        var outerParamName = new LocalName("a", 0);
        var innerParamName = new LocalName("a", 1);

        /* a -> match a with { case a -> a } */
        var originalNode = lambdaNode(
                Range.EMPTY,
                List.of(paramNode(Range.EMPTY, "a")),
                matchNode(
                        Range.EMPTY,
                        refNode(Range.EMPTY, "a"),
                        List.of(
                                caseNode(
                                        Range.EMPTY,
                                        idPatternNode(Range.EMPTY, "a"),
                                        refNode(Range.EMPTY, "a")))));

        var expectedNode = lambdaNode(
                Meta.of(Nameless.INSTANCE),
                List.of(paramNode(Meta.of(outerParamName), "a")),
                matchNode(
                        Meta.of(Nameless.INSTANCE),
                        refNode(Meta.of(outerParamName), "a"),
                        List.of(
                                caseNode(
                                        Meta.of(Nameless.INSTANCE),
                                        idPatternNode(Meta.of(innerParamName), "a"),
                                        refNode(Meta.of(innerParamName), "a")))));

        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameMatchCaseConstructorPatterns() {
        /*-
         * list -> match list with {
         *   case Cons { head } -> Some(head)
         *   case Nil {} -> None()
         * }
         */
        var originalNode = lambdaNode(
                Range.EMPTY,
                List.of(
                        paramNode(Range.EMPTY, "list")),
                matchNode(
                        Range.EMPTY,
                        refNode(Range.EMPTY, "list"),
                        List.of(
                                caseNode(
                                        Range.EMPTY,
                                        constructorPatternNode(
                                                Range.EMPTY,
                                                idNode(Range.EMPTY, "Cons"),
                                                List.of(
                                                        fieldPatternNode(
                                                            Range.EMPTY, "head",
                                                            idPatternNode(Range.EMPTY, "head")))),
                                        applyNode(
                                                Range.EMPTY,
                                                refNode(Range.EMPTY, "Some"),
                                                List.of(refNode(Range.EMPTY, "head")))),
                                caseNode(
                                        Range.EMPTY,
                                        constructorPatternNode(
                                                Range.EMPTY,
                                                idNode(Range.EMPTY, "Nil"),
                                                List.of()),
                                        applyNode(
                                                Range.EMPTY,
                                                refNode(Range.EMPTY, "None"),
                                                List.of())))));

        var namespaceName = new NamespaceName(List.of("Mina", "Test"), "Renamer");
        var lambdaParamName = new LocalName("list", 0);
        var fieldPatName = new LocalName("head", 1);

        var listMeta = Meta.of(new DataName(new QualifiedName(namespaceName, "List")));
        var consMeta = Meta.<Name>of(new ConstructorName(listMeta.meta(), new QualifiedName(namespaceName, "Cons")));
        var nilMeta = Meta.<Name>of(new ConstructorName(listMeta.meta(), new QualifiedName(namespaceName, "Nil")));
        var optionMeta = Meta.of(new DataName(new QualifiedName(namespaceName, "Option")));
        var someMeta = Meta.<Name>of(new ConstructorName(optionMeta.meta(), new QualifiedName(namespaceName, "Some")));
        var noneMeta = Meta.<Name>of(new ConstructorName(optionMeta.meta(), new QualifiedName(namespaceName, "None")));
        var headMeta = Meta.<Name>of(new FieldName((ConstructorName) consMeta.meta(), "head"));

        var expectedNode = lambdaNode(
                Meta.of(Nameless.INSTANCE),
                List.of(
                        paramNode(Meta.of(lambdaParamName), "list")),
                matchNode(
                        Meta.of(Nameless.INSTANCE),
                        refNode(Meta.of(lambdaParamName), "list"),
                        List.of(
                                caseNode(
                                        Meta.of(Nameless.INSTANCE),
                                        constructorPatternNode(
                                                consMeta,
                                                idNode(Range.EMPTY, "Cons"),
                                                List.of(
                                                        fieldPatternNode(
                                                            headMeta, "head",
                                                            idPatternNode(Meta.of(fieldPatName), "head")))),
                                        applyNode(
                                                Meta.of(Nameless.INSTANCE),
                                                refNode(someMeta, "Some"),
                                                List.of(refNode(Meta.of(fieldPatName), "head")))),
                                caseNode(
                                        Meta.of(Nameless.INSTANCE),
                                        constructorPatternNode(
                                                nilMeta,
                                                idNode(Range.EMPTY, "Nil"),
                                                List.of()),
                                        applyNode(
                                                Meta.of(Nameless.INSTANCE),
                                                refNode(noneMeta, "None"),
                                                List.of())))));

        var imports = new ImportedNamesScope();

        imports.putValueIfAbsent("Cons", consMeta);
        imports.putValueIfAbsent("Nil", nilMeta);
        imports.putValueIfAbsent("Some", someMeta);
        imports.putValueIfAbsent("None", noneMeta);

        imports.putFieldIfAbsent((ConstructorName) consMeta.meta(), "head", headMeta);

        var environment = NameEnvironment.of(imports);

        testSuccessfulRename(environment, originalNode, expectedNode);
    }

    @Test
    void renameMatchCaseFieldPattern() {
        /*-
         * list -> match list with {
         *   case Cons { head: first } -> Some(first)
         *   case Nil {} -> None()
         * }
         */
        var originalNode = lambdaNode(
                Range.EMPTY,
                List.of(
                        paramNode(Range.EMPTY, "list")),
                matchNode(
                        Range.EMPTY,
                        refNode(Range.EMPTY, "list"),
                        List.of(
                                caseNode(
                                        Range.EMPTY,
                                        constructorPatternNode(
                                                Range.EMPTY,
                                                idNode(Range.EMPTY, "Cons"),
                                                List.of(
                                                        fieldPatternNode(Range.EMPTY, "head",
                                                                idPatternNode(Range.EMPTY, "first")))),
                                        applyNode(
                                                Range.EMPTY,
                                                refNode(Range.EMPTY, "Some"),
                                                List.of(refNode(Range.EMPTY, "first")))),
                                caseNode(
                                        Range.EMPTY,
                                        constructorPatternNode(
                                                Range.EMPTY,
                                                idNode(Range.EMPTY, "Nil"),
                                                List.of()),
                                        applyNode(
                                                Range.EMPTY,
                                                refNode(Range.EMPTY, "None"),
                                                List.of())))));

        var namespaceName = new NamespaceName(List.of("Mina", "Test"), "Renamer");
        var lambdaParamName = new LocalName("list", 0);
        var idPatName = new LocalName("first", 1);

        var listMeta = Meta.of(new DataName(new QualifiedName(namespaceName, "List")));
        var consMeta = Meta.<Name>of(new ConstructorName(listMeta.meta(), new QualifiedName(namespaceName, "Cons")));
        var nilMeta = Meta.<Name>of(new ConstructorName(listMeta.meta(), new QualifiedName(namespaceName, "Nil")));
        var optionMeta = Meta.of(new DataName(new QualifiedName(namespaceName, "Option")));
        var someMeta = Meta.<Name>of(new ConstructorName(optionMeta.meta(), new QualifiedName(namespaceName, "Some")));
        var noneMeta = Meta.<Name>of(new ConstructorName(optionMeta.meta(), new QualifiedName(namespaceName, "None")));
        var headMeta = Meta.<Name>of(new FieldName((ConstructorName) consMeta.meta(), "head"));

        var expectedNode = lambdaNode(
                Meta.of(Nameless.INSTANCE),
                List.of(
                        paramNode(Meta.of(lambdaParamName), "list")),
                matchNode(
                        Meta.of(Nameless.INSTANCE),
                        refNode(Meta.of(lambdaParamName), "list"),
                        List.of(
                                caseNode(
                                        Meta.of(Nameless.INSTANCE),
                                        constructorPatternNode(
                                                consMeta,
                                                idNode(Range.EMPTY, "Cons"),
                                                List.of(
                                                        fieldPatternNode(
                                                                headMeta, "head",
                                                                idPatternNode(Meta.of(idPatName), "first")))),
                                        applyNode(
                                                Meta.of(Nameless.INSTANCE),
                                                refNode(someMeta, "Some"),
                                                List.of(refNode(Meta.of(idPatName), "first")))),
                                caseNode(
                                        Meta.of(Nameless.INSTANCE),
                                        constructorPatternNode(
                                                nilMeta,
                                                idNode(Range.EMPTY, "Nil"),
                                                List.of()),
                                        applyNode(
                                                Meta.of(Nameless.INSTANCE),
                                                refNode(noneMeta, "None"),
                                                List.of())))));

        var imports = new ImportedNamesScope();

        imports.putValueIfAbsent("Cons", consMeta);
        imports.putValueIfAbsent("Nil", nilMeta);
        imports.putValueIfAbsent("Some", someMeta);
        imports.putValueIfAbsent("None", noneMeta);

        imports.putFieldIfAbsent((ConstructorName) consMeta.meta(), "head", headMeta);

        var environment = NameEnvironment.of(imports);

        testSuccessfulRename(environment, originalNode, expectedNode);
    }

    @Test
    void renameMatchCaseIdPatternShadowsFieldName() {
        /*-
         * list -> match list with {
         *   case Cons { head: head } -> Some(head)
         *   case Nil {} -> None()
         * }
         */
        var originalNode = lambdaNode(
                Range.EMPTY,
                List.of(
                        paramNode(Range.EMPTY, "list")),
                matchNode(
                        Range.EMPTY,
                        refNode(Range.EMPTY, "list"),
                        List.of(
                                caseNode(
                                        Range.EMPTY,
                                        constructorPatternNode(
                                                Range.EMPTY,
                                                idNode(Range.EMPTY, "Cons"),
                                                List.of(
                                                        fieldPatternNode(
                                                            Range.EMPTY, "head",
                                                            idPatternNode(Range.EMPTY, "head")))),
                                        applyNode(
                                                Range.EMPTY,
                                                refNode(Range.EMPTY, "Some"),
                                                List.of(refNode(Range.EMPTY, "head")))),
                                caseNode(
                                        Range.EMPTY,
                                        constructorPatternNode(
                                                Range.EMPTY,
                                                idNode(Range.EMPTY, "Nil"),
                                                List.of()),
                                        applyNode(
                                                Range.EMPTY,
                                                refNode(Range.EMPTY, "None"),
                                                List.of())))));

        var namespaceName = new NamespaceName(List.of("Mina", "Test"), "Renamer");
        var lambdaParamName = new LocalName("list", 0);
        var idPatName = new LocalName("head", 1);

        var listMeta = Meta.of(new DataName(new QualifiedName(namespaceName, "List")));
        var consMeta = Meta.<Name>of(new ConstructorName(listMeta.meta(), new QualifiedName(namespaceName, "Cons")));
        var nilMeta = Meta.<Name>of(new ConstructorName(listMeta.meta(), new QualifiedName(namespaceName, "Nil")));
        var optionMeta = Meta.of(new DataName(new QualifiedName(namespaceName, "Option")));
        var someMeta = Meta.<Name>of(new ConstructorName(optionMeta.meta(), new QualifiedName(namespaceName, "Some")));
        var noneMeta = Meta.<Name>of(new ConstructorName(optionMeta.meta(), new QualifiedName(namespaceName, "None")));
        var headMeta = Meta.<Name>of(new FieldName((ConstructorName) consMeta.meta(), "head"));

        var expectedNode = lambdaNode(
                Meta.of(Nameless.INSTANCE),
                List.of(
                        paramNode(Meta.of(lambdaParamName), "list")),
                matchNode(
                        Meta.of(Nameless.INSTANCE),
                        refNode(Meta.of(lambdaParamName), "list"),
                        List.of(
                                caseNode(
                                        Meta.of(Nameless.INSTANCE),
                                        constructorPatternNode(
                                                consMeta,
                                                idNode(Range.EMPTY, "Cons"),
                                                List.of(
                                                        fieldPatternNode(
                                                                headMeta, "head",
                                                                idPatternNode(Meta.of(idPatName), "head")))),
                                        applyNode(
                                                Meta.of(Nameless.INSTANCE),
                                                refNode(someMeta, "Some"),
                                                List.of(refNode(Meta.of(idPatName), "head")))),
                                caseNode(
                                        Meta.of(Nameless.INSTANCE),
                                        constructorPatternNode(
                                                nilMeta,
                                                idNode(Range.EMPTY, "Nil"),
                                                List.of()),
                                        applyNode(
                                                Meta.of(Nameless.INSTANCE),
                                                refNode(noneMeta, "None"),
                                                List.of())))));

        var imports = new ImportedNamesScope();

        imports.putValueIfAbsent("Cons", consMeta);
        imports.putValueIfAbsent("Nil", nilMeta);
        imports.putValueIfAbsent("Some", someMeta);
        imports.putValueIfAbsent("None", noneMeta);

        imports.putFieldIfAbsent((ConstructorName) consMeta.meta(), "head", headMeta);

        var environment = NameEnvironment.of(imports);

        testSuccessfulRename(environment, originalNode, expectedNode);
    }

    @Test
    void renameMatchCaseUnknownConstructor() {
        var unknownConstructorRange = new Range(1, 7, 1, 10);

        /*-
         * list -> match list with {
         *   case Cons { head } -> Some(head)
         * }
         */
        var originalNode = lambdaNode(
                Range.EMPTY,
                List.of(
                        paramNode(Range.EMPTY, "list")),
                matchNode(
                        Range.EMPTY,
                        refNode(Range.EMPTY, "list"),
                        List.of(
                                caseNode(
                                        Range.EMPTY,
                                        constructorPatternNode(
                                                unknownConstructorRange,
                                                idNode(Range.EMPTY, "Cons"),
                                                List.of(
                                                        fieldPatternNode(
                                                            Range.EMPTY, "head",
                                                            idPatternNode(Range.EMPTY, "head")))),
                                        applyNode(
                                                Range.EMPTY,
                                                refNode(Range.EMPTY, "Some"),
                                                List.of(refNode(Range.EMPTY, "head")))))));

        var collector = testFailedRename(NameEnvironment.empty(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                unknownConstructorRange,
                "Reference to unknown constructor 'Cons'");
    }

    @Test
    void renameMatchCaseUnknownConstructorField() {
        var unknownFieldRange = new Range(1, 14, 1, 16);

        /*-
         * list -> match list with {
         *   case Cons { hed } -> Some(hed)
         * }
         */
        var originalNode = lambdaNode(
                Range.EMPTY,
                List.of(
                        paramNode(Range.EMPTY, "list")),
                matchNode(
                        Range.EMPTY,
                        refNode(Range.EMPTY, "list"),
                        List.of(
                                caseNode(
                                        Range.EMPTY,
                                        constructorPatternNode(
                                                Range.EMPTY,
                                                idNode(Range.EMPTY, "Cons"),
                                                List.of(
                                                        fieldPatternNode(
                                                            unknownFieldRange, "hed",
                                                            idPatternNode(unknownFieldRange, "head")))),
                                        applyNode(
                                                Range.EMPTY,
                                                refNode(Range.EMPTY, "Some"),
                                                List.of(refNode(Range.EMPTY, "hed")))))));

        var namespaceName = new NamespaceName(List.of("Mina", "Test"), "Renamer");
        var listMeta = Meta.of(new DataName(new QualifiedName(namespaceName, "List")));
        var consMeta = Meta.<Name>of(new ConstructorName(listMeta.meta(), new QualifiedName(namespaceName, "Cons")));
        var nilMeta = Meta.<Name>of(new ConstructorName(listMeta.meta(), new QualifiedName(namespaceName, "Nil")));
        var optionMeta = Meta.of(new DataName(new QualifiedName(namespaceName, "Option")));
        var someMeta = Meta.<Name>of(new ConstructorName(optionMeta.meta(), new QualifiedName(namespaceName, "Some")));
        var noneMeta = Meta.<Name>of(new ConstructorName(optionMeta.meta(), new QualifiedName(namespaceName, "None")));
        var headMeta = Meta.<Name>of(new FieldName((ConstructorName) consMeta.meta(), "head"));

        var imports = new ImportedNamesScope();

        imports.putValueIfAbsent("Cons", consMeta);
        imports.putValueIfAbsent("Nil", nilMeta);
        imports.putValueIfAbsent("Some", someMeta);
        imports.putValueIfAbsent("None", noneMeta);

        imports.putFieldIfAbsent((ConstructorName) consMeta.meta(), "head", headMeta);

        var environment = NameEnvironment.of(imports);

        var collector = testFailedRename(environment, originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                unknownFieldRange,
                "Reference to unknown field 'hed' in constructor 'Mina/Test/Renamer.Cons'");
    }

    @Test
    void warnWhenLocalValueShadowsImport() {
        var importedSymbolRange = new Range(1, 25, 1, 27);
        var localDeclarationRange = new Range(2, 2, 2, 12);

        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var namespaceName = new NamespaceName(List.of("Mina", "Test"), "Renamer");
        var letName = new LetName(new QualifiedName(namespaceName, "one"));

        var importedIdNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Other");
        var importedNamespaceName = new NamespaceName(List.of("Mina", "Test"), "Other");
        var importedSymbolName = new LetName(new QualifiedName(importedNamespaceName, "one"));

        /*-
         * namespace Mina/Test/Renamer {
         *   import Mina/Test/Other.one
         *   let one = 1
         * }
         */
        var originalNode = namespaceNode(
            Range.EMPTY, idNode,
            List.of(importSymbolsNode(Range.EMPTY, importedIdNode, importeeNode(importedSymbolRange, "one"))),
            List.of(letNode(localDeclarationRange, "one", intNode(Range.EMPTY, 1))));

        var expectedNode = namespaceNode(
            Meta.of(namespaceName), idNode,
            List.of(importSymbolsNode(Range.EMPTY, importedIdNode, importeeNode(importedSymbolRange, "one"))),
            List.of(letNode(new Meta<>(localDeclarationRange, letName), "one", intNode(Meta.of(Nameless.INSTANCE), 1))));

        var environment = NameEnvironment.withBuiltInNames();

        environment.putValue("one", new Meta<>(importedSymbolRange, importedSymbolName));

        var collector = testRenameWithWarnings(environment, originalNode, expectedNode);

        assertDiagnosticWithRelatedInfo(
            collector.getDiagnostics(),
            localDeclarationRange,
            "The local declaration 'one' shadows the imported declaration 'Mina/Test/Other.one'",
            importedSymbolRange,
            "Import of declaration 'one'");
    }

    @Test
    void warnWhenLocalTypeShadowsImport() {
        var importedSymbolRange = new Range(1, 25, 1, 28);
        var localDeclarationRange = new Range(2, 2, 2, 13);

        var idNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Renamer");

        var namespaceName = new NamespaceName(List.of("Mina", "Test"), "Renamer");
        var dataName = new DataName(new QualifiedName(namespaceName, "Void"));

        var importedIdNode = nsIdNode(Range.EMPTY, List.of("Mina", "Test"), "Other");
        var importedNamespaceName = new NamespaceName(List.of("Mina", "Test"), "Other");
        var importedSymbolName = new DataName(new QualifiedName(importedNamespaceName, "Void"));

        /*-
         * namespace Mina/Test/Renamer {
         *   import Mina/Test/Other.Void
         *   data Void {}
         * }
         */
        var originalNode = namespaceNode(
            Range.EMPTY, idNode,
            List.of(importSymbolsNode(Range.EMPTY, importedIdNode, importeeNode(importedSymbolRange, "Void"))),
            List.of(dataNode(localDeclarationRange, "Void", List.of(), List.of())));

        var expectedNode = namespaceNode(
            Meta.of(namespaceName), idNode,
            List.of(importSymbolsNode(Range.EMPTY, importedIdNode, importeeNode(importedSymbolRange, "Void"))),
            List.of(dataNode(new Meta<Name>(localDeclarationRange, dataName), "Void", List.of(), List.of())));

        var environment = NameEnvironment.withBuiltInNames();

        environment.putType("Void", new Meta<>(importedSymbolRange, importedSymbolName));

        var collector = testRenameWithWarnings(environment, originalNode, expectedNode);

        assertDiagnosticWithRelatedInfo(
            collector.getDiagnostics(),
            localDeclarationRange,
            "The local declaration 'Void' shadows the imported declaration 'Mina/Test/Other.Void'",
            importedSymbolRange,
            "Import of declaration 'Void'");
    }

    @Test
    void renameLiteralBoolean() {
        var originalNode = boolNode(Range.EMPTY, true);
        var expectedNode = boolNode(Meta.of(Nameless.INSTANCE), true);
        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLiteralChar() {
        var originalNode = charNode(Range.EMPTY, 'a');
        var expectedNode = charNode(Meta.of(Nameless.INSTANCE), 'a');
        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLiteralString() {
        var originalNode = stringNode(Range.EMPTY, "abc");
        var expectedNode = stringNode(Meta.of(Nameless.INSTANCE), "abc");
        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLiteralInt() {
        var originalNode = intNode(Range.EMPTY, 1);
        var expectedNode = intNode(Meta.of(Nameless.INSTANCE), 1);
        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLiteralLong() {
        var originalNode = longNode(Range.EMPTY, 1L);
        var expectedNode = longNode(Meta.of(Nameless.INSTANCE), 1L);
        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLiteralFloat() {
        var originalNode = floatNode(Range.EMPTY, 0.1f);
        var expectedNode = floatNode(Meta.of(Nameless.INSTANCE), 0.1f);
        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }

    @Test
    void renameLiteralDouble() {
        var originalNode = doubleNode(Range.EMPTY, 0.1);
        var expectedNode = doubleNode(Meta.of(Nameless.INSTANCE), 0.1);
        testSuccessfulRename(NameEnvironment.empty(), originalNode, expectedNode);
    }
}

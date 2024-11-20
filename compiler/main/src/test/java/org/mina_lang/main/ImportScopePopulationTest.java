/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.main;

import com.opencastsoftware.yvette.Range;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Scope;
import org.mina_lang.common.TopLevelScope;
import org.mina_lang.common.diagnostics.Diagnostic;
import org.mina_lang.common.diagnostics.NamespaceDiagnosticReporter;
import org.mina_lang.common.names.*;
import org.mina_lang.common.types.BuiltInType;
import org.mina_lang.common.types.Type;
import org.mina_lang.common.types.TypeConstructor;
import org.mina_lang.common.types.TypeKind;
import org.mina_lang.renamer.scopes.ImportedNamesScope;
import org.mina_lang.syntax.NamespaceNode;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mina_lang.syntax.SyntaxNodes.*;

public class ImportScopePopulationTest {
    private ImportScopePopulation<Name, ImportedNamesScope> createScopePopulator(
        Map<NamespaceName, NamespaceNode<Name>> namespaceNode,
        Map<NamespaceName, Scope<Attributes>> classpathScope,
        Map<NamespaceName, NamespaceDiagnosticReporter> diagnostics) {
        return new ImportScopePopulation<>() {
            @Override
            public Optional<NamespaceNode<Name>> getNamespaceNode(NamespaceName namespaceName) {
                return Optional.ofNullable(namespaceNode.get(namespaceName));
            }

            @Override
            public NamespaceDiagnosticReporter getNamespaceDiagnostics(NamespaceName namespaceName) {
                return diagnostics.get(namespaceName);
            }

            @Override
            public Optional<Scope<Attributes>> getClasspathScope(NamespaceName namespaceName) {
                return Optional.ofNullable(classpathScope.get(namespaceName));
            }

            @Override
            public Named getName(Meta<Name> meta) {
                return (Named) meta.meta();
            }

            @Override
            public Meta<Name> transformMeta(Meta<Attributes> meta) {
                return new Meta<>(meta.range(), meta.meta().name());
            }
        };
    }

    void assertDiagnostic(List<Diagnostic> diagnostics, Range range, String message) {
        assertThat(diagnostics, is(not(empty())));
        var firstDiagnostic = diagnostics.get(0);
        assertThat(firstDiagnostic.message(), is(equalTo(message)));
        assertThat(firstDiagnostic.location().range(), is(equalTo(range)));
        assertThat(firstDiagnostic.relatedInformation().toList(), is(empty()));
    }

    void assertDiagnosticWithRelatedInfo(List<Diagnostic> diagnostics, Range diagnosticRange, String diagnosticMessage,
                                         Range relatedInfoRange, String relatedInfoMessage) {
        assertThat(diagnostics, is(not(empty())));

        var firstDiagnostic = diagnostics.get(0);
        assertThat(firstDiagnostic.message(), is(equalTo(diagnosticMessage)));
        assertThat(firstDiagnostic.location().range(), is(equalTo(diagnosticRange)));

        assertThat(firstDiagnostic.relatedInformation().toList(), is(not(empty())));

        var firstRelatedInfo = firstDiagnostic.relatedInformation().get(0);
        assertThat(firstRelatedInfo.message(), startsWith(relatedInfoMessage));
        assertThat(firstRelatedInfo.location().range(), is(equalTo(relatedInfoRange)));
    }

    @Test
    void failsWhenUnableToFindQualifiedImportNamespace() {
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Renamer");
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Renamer.mina");
        var scopedCollector = Map.of(nsName, new NamespaceDiagnosticReporter(baseCollector, dummyUri));

        var importedNsRange = new Range(1, 2, 1, 23);
        var idNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Renamer");
        var importedIdNode = nsIdNode(importedNsRange, Lists.immutable.of("Mina", "Test"), "Other");

        /*-
         * namespace Mina/Test/Renamer {
         *   import Mina/Test/Other
         * }
         */
        var namespaceNode = namespaceNode(
            Range.EMPTY, idNode,
            Lists.immutable.of(importQualifiedNode(Range.EMPTY, importedIdNode)),
            Lists.immutable.empty());

        var populator = createScopePopulator(
            Maps.mutable.empty(),
            Maps.mutable.empty(),
            scopedCollector);

        assertThat(populator.populateImportScope(namespaceNode, new ImportedNamesScope()), is(Optional.empty()));

        assertDiagnostic(
            baseCollector.getDiagnostics(),
            importedNsRange,
            "The namespace 'Mina/Test/Other' could not be found.");
    }

    @Test
    void failsWhenUnableToFindImportedSymbol() {
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Renamer");
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Renamer.mina");
        var scopedCollector = Map.of(nsName, new NamespaceDiagnosticReporter(baseCollector, dummyUri));

        var idNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Renamer");
        var importedIdNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Other");
        var importedNsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Other");
        var importedSymbolRange = new Range(1, 25, 1, 28);

        var importedScope = new TopLevelScope<Attributes>(
            Maps.mutable.empty(),
            Maps.mutable.empty(),
            Maps.mutable.empty()
        );

        /*-
         * namespace Mina/Test/Renamer {
         *   import Mina/Test/Other.Void
         * }
         */
        var namespaceNode = namespaceNode(
            Range.EMPTY, idNode,
            Lists.immutable.of(
                importSymbolsNode(
                    Range.EMPTY, importedIdNode,
                    importeeNode(importedSymbolRange, "Void"))),
            Lists.immutable.empty());

        var populator = createScopePopulator(
            Maps.mutable.empty(),
            Map.of(importedNsName, importedScope),
            scopedCollector);

        assertThat(populator.populateImportScope(namespaceNode, new ImportedNamesScope()), is(Optional.empty()));

        assertDiagnostic(
            baseCollector.getDiagnostics(),
            importedSymbolRange,
            "The symbol 'Void' could not be found in namespace 'Mina/Test/Other'.");
    }

    @Test
    void failsWhenDuplicateValueImported() {
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Renamer");
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Renamer.mina");
        var scopedCollector = Map.of(nsName, new NamespaceDiagnosticReporter(baseCollector, dummyUri));

        var idNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Renamer");
        var importedIdNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Other");
        var importedNsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Other");
        var importedSymbolRange = new Range(1, 25, 1, 28);

        var duplicateIdNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Duplicate");
        var duplicateNsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Duplicate");
        var duplicateSymbolRange = new Range(2, 29, 2, 33);

        var importedScope = new TopLevelScope<>(
            Maps.mutable.of("one", Meta.of(
                new LetName(new QualifiedName(duplicateNsName, "one")),
                new BuiltInType("Int", TypeKind.INSTANCE))),
            Maps.mutable.empty(),
            Maps.mutable.empty()
        );

        var duplicateScope = new TopLevelScope<>(
            Maps.mutable.of("one", Meta.of(
                new LetName(new QualifiedName(duplicateNsName, "one")),
                new BuiltInType("Int", TypeKind.INSTANCE))),
            Maps.mutable.empty(),
            Maps.mutable.empty()
        );

        /*-
         * namespace Mina/Test/Renamer {
         *   import Mina/Test/Other.one
         *   import Mina/Test/Duplicate.one
         * }
         */
        var namespaceNode = namespaceNode(
            Range.EMPTY, idNode,
            Lists.immutable.of(
                importSymbolsNode(Range.EMPTY, importedIdNode, importeeNode(importedSymbolRange, "one")),
                importSymbolsNode(Range.EMPTY, duplicateIdNode, importeeNode(duplicateSymbolRange, "one"))),
            Lists.immutable.empty());

        var populator = createScopePopulator(
            Maps.mutable.empty(),
            Maps.mutable.of(
                importedNsName, importedScope,
                duplicateNsName, duplicateScope),
            scopedCollector);

        assertThat(
            populator.populateImportScope(namespaceNode, new ImportedNamesScope()),
            is(Optional.empty()));

        assertDiagnosticWithRelatedInfo(
            baseCollector.getDiagnostics(),
            duplicateSymbolRange,
            "Duplicate definition of value 'one'",
            importedSymbolRange,
            "Original definition of value 'one'");
    }

    @Test
    void failsWhenDuplicateTypeImported() {
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Renamer");
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Renamer.mina");
        var scopedCollector = Map.of(nsName, new NamespaceDiagnosticReporter(baseCollector, dummyUri));

        var idNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Renamer");
        var importedIdNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Other");
        var importedNsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Other");
        var importedSymbolRange = new Range(1, 25, 1, 28);

        var duplicateIdNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Duplicate");
        var duplicateNsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Duplicate");
        var duplicateSymbolRange = new Range(2, 29, 2, 33);

        var importedScope = new TopLevelScope<>(
            Maps.mutable.empty(),
            Maps.mutable.of("Void", Meta.of(
                new DataName(new QualifiedName(importedNsName, "Void")),
                new TypeConstructor(new QualifiedName(importedNsName, "Void"), TypeKind.INSTANCE))),
            Maps.mutable.empty()
        );

        var duplicateScope = new TopLevelScope<>(
            Maps.mutable.empty(),
            Maps.mutable.of("Void", Meta.of(
                new DataName(new QualifiedName(duplicateNsName, "Void")),
                new TypeConstructor(new QualifiedName(duplicateNsName, "Void"), TypeKind.INSTANCE))),
            Maps.mutable.empty()
        );

        /*-
         * namespace Mina/Test/Renamer {
         *   import Mina/Test/Other.Void
         *   import Mina/Test/Duplicate.Void
         * }
         */
        var namespaceNode = namespaceNode(
            Range.EMPTY, idNode,
            Lists.immutable.of(
                importSymbolsNode(Range.EMPTY, importedIdNode, importeeNode(importedSymbolRange, "Void")),
                importSymbolsNode(Range.EMPTY, duplicateIdNode, importeeNode(duplicateSymbolRange, "Void"))),
            Lists.immutable.empty());

        var populator = createScopePopulator(
            Maps.mutable.empty(),
            Maps.mutable.of(
                importedNsName, importedScope,
                duplicateNsName, duplicateScope),
            scopedCollector);

        assertThat(
            populator.populateImportScope(namespaceNode, new ImportedNamesScope()),
            is(Optional.empty()));

        assertDiagnosticWithRelatedInfo(
            baseCollector.getDiagnostics(),
            duplicateSymbolRange,
            "Duplicate definition of type 'Void'",
            importedSymbolRange,
            "Original definition of type 'Void'");
    }

    @Test
    void populatesQualifiedImportsSuccessfully() {
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Renamer");
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Renamer.mina");
        var scopedCollector = Map.of(nsName, new NamespaceDiagnosticReporter(baseCollector, dummyUri));

        var idNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Renamer");
        var importedIdNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Other");
        var importedNsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Other");
        var importedNsRange = new Range(1, 2, 1, 23);

        var importedScope = new TopLevelScope<>(
            Maps.mutable.of("one", Meta.of(
                new LetName(new QualifiedName(importedNsName, "one")),
                new BuiltInType("Int", TypeKind.INSTANCE))),
            Maps.mutable.of("Void", Meta.of(
                new DataName(new QualifiedName(importedNsName, "Void")),
                new TypeConstructor(new QualifiedName(importedNsName, "Void"), TypeKind.INSTANCE))),
            Maps.mutable.empty()
        );

        /*-
         * namespace Mina/Test/Renamer {
         *   import Mina/Test/Other
         * }
         */
        var namespaceNode = namespaceNode(
            Range.EMPTY, idNode,
            Lists.immutable.of(importQualifiedNode(importedNsRange, importedIdNode)),
            Lists.immutable.empty());

        var populator = createScopePopulator(
            Maps.mutable.empty(),
            Maps.mutable.of(importedNsName, importedScope),
            scopedCollector);

        var expectedScope = new ImportedNamesScope();
        expectedScope.putValue("Other.one", new Meta<>(
            importedNsRange,
            new LetName(new QualifiedName(importedNsName, "one"))));
        expectedScope.putType("Other.Void", new Meta<>(
            importedNsRange,
            new DataName(new QualifiedName(importedNsName, "Void"))));

        assertThat(
            populator.populateImportScope(namespaceNode, new ImportedNamesScope()),
            is(Optional.of(expectedScope)));

        assertThat(baseCollector.getDiagnostics(), is(empty()));
    }

    @Test
    void populatesQualifiedImportWithConstructorFieldsSuccessfully() {
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Renamer");
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Renamer.mina");
        var scopedCollector = Map.of(nsName, new NamespaceDiagnosticReporter(baseCollector, dummyUri));

        var idNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Renamer");
        var importedIdNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Other");
        var importedNsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Other");
        var importedNsRange = new Range(1, 2, 1, 23);

        var dataName = new DataName(new QualifiedName(importedNsName, "One"));
        var constrName = new ConstructorName(dataName, new QualifiedName(importedNsName, "MkOne"));

        var importedScope = new TopLevelScope<>(
            Maps.mutable.of(
                "one", Meta.of(
                    new LetName(new QualifiedName(importedNsName, "one")),
                    new BuiltInType("Int", TypeKind.INSTANCE)),
                "MkOne", Meta.of(
                    constrName,
                    Type.function(
                        Type.INT,
                        new TypeConstructor(new QualifiedName(importedNsName, "One"), TypeKind.INSTANCE)))),
            Maps.mutable.of("One", Meta.of(
                dataName,
                new TypeConstructor(new QualifiedName(importedNsName, "One"), TypeKind.INSTANCE))),
            Maps.mutable.of(constrName, Maps.mutable.of(
                    "value",
                    Meta.of(
                        new FieldName(constrName, "value"),
                        new BuiltInType("Int", TypeKind.INSTANCE))))
        );

        /*-
         * namespace Mina/Test/Renamer {
         *   import Mina/Test/Other
         * }
         */
        var namespaceNode = namespaceNode(
            Range.EMPTY, idNode,
            Lists.immutable.of(importQualifiedNode(importedNsRange, importedIdNode)),
            Lists.immutable.empty());

        var populator = createScopePopulator(
            Maps.mutable.empty(),
            Maps.mutable.of(importedNsName, importedScope),
            scopedCollector);

        var expectedScope = new ImportedNamesScope();
        expectedScope.putValue("Other.one", new Meta<>(
            importedNsRange,
            new LetName(new QualifiedName(importedNsName, "one"))));
        expectedScope.putValue("Other.MkOne",
            new Meta<>(importedNsRange, constrName));
        expectedScope.putType("Other.One", new Meta<>(
            importedNsRange,
            new DataName(new QualifiedName(importedNsName, "One"))));
        expectedScope.putField(constrName, "value", new Meta<>(
            Range.EMPTY,
            new FieldName(constrName, "value")));

        assertThat(
            populator.populateImportScope(namespaceNode, new ImportedNamesScope()),
            is(Optional.of(expectedScope)));

        assertThat(baseCollector.getDiagnostics(), is(empty()));
    }

    @Test
    void populatesQualifiedImportsWithAliasSuccessfully() {
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Renamer");
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Renamer.mina");
        var scopedCollector = Map.of(nsName, new NamespaceDiagnosticReporter(baseCollector, dummyUri));

        var idNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Renamer");
        var importedIdNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Other");
        var importedNsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Other");
        var importedNsRange = new Range(1, 2, 1, 23);

        var importedScope = new TopLevelScope<>(
            Maps.mutable.of("one", Meta.of(
                new LetName(new QualifiedName(importedNsName, "one")),
                new BuiltInType("Int", TypeKind.INSTANCE))),
            Maps.mutable.of("Void", Meta.of(
                new DataName(new QualifiedName(importedNsName, "Void")),
                new TypeConstructor(new QualifiedName(importedNsName, "Void"), TypeKind.INSTANCE))),
            Maps.mutable.empty()
        );

        /*-
         * namespace Mina/Test/Renamer {
         *   import Mina/Test/Other as Another
         * }
         */
        var namespaceNode = namespaceNode(
            Range.EMPTY, idNode,
            Lists.immutable.of(
                importQualifiedNode(importedNsRange, importedIdNode, Optional.of("Another"))),
            Lists.immutable.empty());

        var populator = createScopePopulator(
            Maps.mutable.empty(),
            Maps.mutable.of(importedNsName, importedScope),
            scopedCollector);

        var expectedScope = new ImportedNamesScope();
        expectedScope.putValue("Another.one", new Meta<>(
            importedNsRange,
            new LetName(new QualifiedName(importedNsName, "one"))));
        expectedScope.putType("Another.Void", new Meta<>(
            importedNsRange,
            new DataName(new QualifiedName(importedNsName, "Void"))));

        assertThat(
            populator.populateImportScope(namespaceNode, new ImportedNamesScope()),
            is(Optional.of(expectedScope)));

        assertThat(baseCollector.getDiagnostics(), is(empty()));
    }

    @Test
    void resolvesValuesFromImportsSuccessfully() {
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Renamer");
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Renamer.mina");
        var scopedCollector = Map.of(nsName, new NamespaceDiagnosticReporter(baseCollector, dummyUri));

        var idNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Renamer");
        var importedIdNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Other");
        var importedNsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Other");
        var importedSymbolRange = new Range(1, 25, 1, 27);

        var importedScope = new TopLevelScope<>(
            Maps.mutable.of("one", Meta.of(
                new LetName(new QualifiedName(importedNsName, "one")),
                new BuiltInType("Int", TypeKind.INSTANCE))),
            Maps.mutable.empty(),
            Maps.mutable.empty()
        );

        /*-
         * namespace Mina/Test/Renamer {
         *   import Mina/Test/Other.one
         * }
         */
        var namespaceNode = namespaceNode(
            Range.EMPTY, idNode,
            Lists.immutable.of(
                importSymbolsNode(
                    Range.EMPTY, importedIdNode,
                    importeeNode(importedSymbolRange, "one"))),
            Lists.immutable.empty());

        var populator = createScopePopulator(
            Maps.mutable.empty(),
            Maps.mutable.of(importedNsName, importedScope),
            scopedCollector);

        var expectedScope = new ImportedNamesScope();
        expectedScope.putValue("one", new Meta<>(
            importedSymbolRange,
            new LetName(new QualifiedName(importedNsName, "one"))));

        assertThat(
            populator.populateImportScope(namespaceNode, new ImportedNamesScope()),
            is(Optional.of(expectedScope)));

        assertThat(baseCollector.getDiagnostics(), is(empty()));
    }

    @Test
    void resolvesAliasedValuesFromImportsSuccessfully() {
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Renamer");
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Renamer.mina");
        var scopedCollector = Map.of(nsName, new NamespaceDiagnosticReporter(baseCollector, dummyUri));

        var idNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Renamer");
        var importedIdNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Other");
        var importedNsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Other");
        var importedSymbolRange = new Range(1, 25, 1, 27);

        var importedScope = new TopLevelScope<>(
            Maps.mutable.of("one", Meta.of(
                new LetName(new QualifiedName(importedNsName, "one")),
                new BuiltInType("Int", TypeKind.INSTANCE))),
            Maps.mutable.empty(),
            Maps.mutable.empty()
        );

        /*-
         * namespace Mina/Test/Renamer {
         *   import Mina/Test/Other.one as One
         * }
         */
        var namespaceNode = namespaceNode(
            Range.EMPTY, idNode,
            Lists.immutable.of(
                importSymbolsNode(
                    Range.EMPTY, importedIdNode,
                    importeeNode(importedSymbolRange, "one", Optional.of("One")))),
            Lists.immutable.empty());

        var populator = createScopePopulator(
            Maps.mutable.empty(),
            Maps.mutable.of(importedNsName, importedScope),
            scopedCollector);

        var expectedScope = new ImportedNamesScope();
        expectedScope.putValue("One", new Meta<>(
            importedSymbolRange,
            new LetName(new QualifiedName(importedNsName, "one"))));

        assertThat(
            populator.populateImportScope(namespaceNode, new ImportedNamesScope()),
            is(Optional.of(expectedScope)));

        assertThat(baseCollector.getDiagnostics(), is(empty()));
    }

    @Test
    void resolvesValuesWithConstructorFieldsSuccessfully() {
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Renamer");
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Renamer.mina");
        var scopedCollector = Map.of(nsName, new NamespaceDiagnosticReporter(baseCollector, dummyUri));

        var idNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Renamer");
        var importedIdNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Other");
        var importedNsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Other");
        var importedSymbolRange = new Range(1, 25, 1, 29);

        var dataName = new DataName(new QualifiedName(importedNsName, "One"));
        var constrName = new ConstructorName(dataName, new QualifiedName(importedNsName, "MkOne"));

        var importedScope = new TopLevelScope<>(
            Maps.mutable.of("MkOne", Meta.of(
                    constrName,
                    Type.function(
                        Type.INT,
                        new TypeConstructor(new QualifiedName(importedNsName, "One"), TypeKind.INSTANCE)))),
            Maps.mutable.of("One", Meta.of(
                dataName,
                new TypeConstructor(new QualifiedName(importedNsName, "One"), TypeKind.INSTANCE))),
            Maps.mutable.of(constrName, Maps.mutable.of(
                "value",
                Meta.of(
                    new FieldName(constrName, "value"),
                    new BuiltInType("Int", TypeKind.INSTANCE))))
        );

        /*-
         * namespace Mina/Test/Renamer {
         *   import Mina/Test/Other.MkOne
         * }
         */
        var namespaceNode = namespaceNode(
            Range.EMPTY, idNode,
            Lists.immutable.of(
                importSymbolsNode(
                    Range.EMPTY, importedIdNode,
                    importeeNode(importedSymbolRange, "MkOne"))),
            Lists.immutable.empty());

        var populator = createScopePopulator(
            Maps.mutable.empty(),
            Maps.mutable.of(importedNsName, importedScope),
            scopedCollector);

        var expectedScope = new ImportedNamesScope();
        expectedScope.putValue("MkOne",
            new Meta<>(importedSymbolRange, constrName));
        expectedScope.putField(constrName, "value", new Meta<>(
            Range.EMPTY,
            new FieldName(constrName, "value")));

        assertThat(
            populator.populateImportScope(namespaceNode, new ImportedNamesScope()),
            is(Optional.of(expectedScope)));

        assertThat(baseCollector.getDiagnostics(), is(empty()));
    }

    @Test
    void resolvesTypesFromImportsSuccessfully() {
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Renamer");
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Renamer.mina");
        var scopedCollector = Map.of(nsName, new NamespaceDiagnosticReporter(baseCollector, dummyUri));

        var idNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Renamer");
        var importedIdNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Other");
        var importedNsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Other");
        var importedSymbolRange = new Range(1, 25, 1, 28);

        var importedScope = new TopLevelScope<>(
            Maps.mutable.empty(),
            Maps.mutable.of("Void", Meta.of(
                new DataName(new QualifiedName(importedNsName, "Void")),
                new TypeConstructor(new QualifiedName(importedNsName, "Void"), TypeKind.INSTANCE))),
            Maps.mutable.empty()
        );

        /*-
         * namespace Mina/Test/Renamer {
         *   import Mina/Test/Other.Void
         * }
         */
        var namespaceNode = namespaceNode(
            Range.EMPTY, idNode,
            Lists.immutable.of(
                importSymbolsNode(
                    Range.EMPTY, importedIdNode,
                    importeeNode(importedSymbolRange, "Void"))),
            Lists.immutable.empty());

        var populator = createScopePopulator(
            Maps.mutable.empty(),
            Maps.mutable.of(importedNsName, importedScope),
            scopedCollector);

        var expectedScope = new ImportedNamesScope();
        expectedScope.putType("Void", new Meta<>(
            importedSymbolRange,
            new DataName(new QualifiedName(importedNsName, "Void"))));

        assertThat(
            populator.populateImportScope(namespaceNode, new ImportedNamesScope()),
            is(Optional.of(expectedScope)));

        assertThat(baseCollector.getDiagnostics(), is(empty()));
    }

    @Test
    void resolvesAliasedTypesFromImportsSuccessfully() {
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Renamer");
        var baseCollector = new ErrorCollector();
        var dummyUri = URI.create("file:///Mina/Test/Renamer.mina");
        var scopedCollector = Map.of(nsName, new NamespaceDiagnosticReporter(baseCollector, dummyUri));

        var idNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Renamer");
        var importedIdNode = nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Other");
        var importedNsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Other");
        var importedSymbolRange = new Range(1, 25, 1, 28);

        var importedScope = new TopLevelScope<>(
            Maps.mutable.empty(),
            Maps.mutable.of("Void", Meta.of(
                new DataName(new QualifiedName(importedNsName, "Void")),
                new TypeConstructor(new QualifiedName(importedNsName, "Void"), TypeKind.INSTANCE))),
            Maps.mutable.empty()
        );

        /*-
         * namespace Mina/Test/Renamer {
         *   import Mina/Test/Other.Void as Never
         * }
         */
        var namespaceNode = namespaceNode(
            Range.EMPTY, idNode,
            Lists.immutable.of(
                importSymbolsNode(
                    Range.EMPTY, importedIdNode,
                    importeeNode(importedSymbolRange, "Void", Optional.of("Never")))),
            Lists.immutable.empty());

        var populator = createScopePopulator(
            Maps.mutable.empty(),
            Maps.mutable.of(importedNsName, importedScope),
            scopedCollector);

        var expectedScope = new ImportedNamesScope();
        expectedScope.putType("Never", new Meta<>(
            importedSymbolRange,
            new DataName(new QualifiedName(importedNsName, "Void"))));

        assertThat(
            populator.populateImportScope(namespaceNode, new ImportedNamesScope()),
            is(Optional.of(expectedScope)));

        assertThat(baseCollector.getDiagnostics(), is(empty()));
    }
}

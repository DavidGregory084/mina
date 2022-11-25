package org.mina_lang.typechecker;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mina_lang.syntax.SyntaxNodes.*;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Test;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Environment;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Range;
import org.mina_lang.common.diagnostics.Diagnostic;
import org.mina_lang.common.names.*;
import org.mina_lang.common.scopes.BuiltInScope;
import org.mina_lang.common.scopes.ImportedScope;
import org.mina_lang.common.types.HigherKind;
import org.mina_lang.common.types.TypeKind;
import org.mina_lang.common.types.UnsolvedVariableSupply;
import org.mina_lang.syntax.TypeNode;

import net.jqwik.api.*;

public class KindcheckerTest {
    void testSuccessfulKindcheck(
            Environment<Attributes> environment,
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
            Environment<Attributes> environment,
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
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Kindchecker");
        var listName = new QualifiedName(nsName, "List");
        var listDataName = new DataName(listName);
        var listKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        var listAttrs = new Attributes(listDataName, listKind);
        var listMeta = new Meta<Attributes>(Range.EMPTY, listAttrs);

        var listMetaWithoutType = listMeta.withMeta(listMeta.meta().name());

        var intAttrs = new Attributes(new BuiltInName("Int"), TypeKind.INSTANCE);
        var intMeta = new Meta<Attributes>(Range.EMPTY, intAttrs);

        var intMetaWithoutType = intMeta.withMeta(intMeta.meta().name());

        var environment = Environment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.populateType(listDataName.localName(), listMeta);
        environment.populateType(listDataName.canonicalName(), listMeta);

        var originalNode = typeApplyNode(
                new Meta<Name>(Range.EMPTY, Nameless.INSTANCE),
                typeRefNode(listMetaWithoutType,
                        idNode(Range.EMPTY, nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Kindchecker"),
                                "List")),
                Lists.immutable.of(typeRefNode(intMetaWithoutType, "Int")));

        var expectedNode = typeApplyNode(
                new Meta<Attributes>(Range.EMPTY, new Attributes(Nameless.INSTANCE, TypeKind.INSTANCE)),
                typeRefNode(listMeta,
                        idNode(Range.EMPTY, nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Kindchecker"),
                                "List")),
                Lists.immutable.of(typeRefNode(intMeta, "Int")));

        testSuccessfulKindcheck(environment, originalNode, expectedNode);
    }

    @Test
    void kindcheckIllKindedListTypeApply() {
        var applyRange = new Range(0, 1, 0, 1);

        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Kindchecker");
        var listName = new QualifiedName(nsName, "List");
        var listDataName = new DataName(listName);
        var listKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        var listAttrs = new Attributes(listDataName, listKind);
        var listMeta = new Meta<Attributes>(Range.EMPTY, listAttrs);

        var listMetaWithoutType = listMeta.withMeta(listMeta.meta().name());

        var intAttrs = new Attributes(new BuiltInName("Int"), TypeKind.INSTANCE);
        var intMeta = new Meta<Attributes>(Range.EMPTY, intAttrs);

        var intMetaWithoutType = intMeta.withMeta(intMeta.meta().name());

        var stringAttrs = new Attributes(new BuiltInName("String"), TypeKind.INSTANCE);
        var stringMeta = new Meta<Attributes>(Range.EMPTY, stringAttrs);

        var stringMetaWithoutType = stringMeta.withMeta(stringMeta.meta().name());

        var environment = Environment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.populateType(listDataName.localName(), listMeta);
        environment.populateType(listDataName.canonicalName(), listMeta);

        var originalNode = typeApplyNode(
                new Meta<Name>(applyRange, Nameless.INSTANCE),
                typeRefNode(listMetaWithoutType,
                        idNode(Range.EMPTY, nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Kindchecker"),
                                "List")),
                Lists.immutable.of(
                        typeRefNode(intMetaWithoutType, "Int"),
                        typeRefNode(stringMetaWithoutType, "String")));

        var collector = testFailedKindcheck(environment, originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                applyRange,
                "Mismatched type application! Expected: * -> *, Actual: (A1, B1) -> C1");
    }

    @Test
    void kindcheckEitherTypeApply() {
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Kindchecker");
        var eitherName = new QualifiedName(nsName, "Either");
        var eitherDataName = new DataName(eitherName);
        var eitherKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE, TypeKind.INSTANCE);
        var eitherAttrs = new Attributes(eitherDataName, eitherKind);
        var eitherMeta = new Meta<Attributes>(Range.EMPTY, eitherAttrs);

        var eitherMetaWithoutType = eitherMeta.withMeta(eitherMeta.meta().name());

        var intAttrs = new Attributes(new BuiltInName("Int"), TypeKind.INSTANCE);
        var intMeta = new Meta<Attributes>(Range.EMPTY, intAttrs);

        var intMetaWithoutType = intMeta.withMeta(intMeta.meta().name());

        var stringAttrs = new Attributes(new BuiltInName("String"), TypeKind.INSTANCE);
        var stringMeta = new Meta<Attributes>(Range.EMPTY, stringAttrs);

        var stringMetaWithoutType = stringMeta.withMeta(stringMeta.meta().name());

        var environment = Environment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.populateType(eitherDataName.localName(), eitherMeta);
        environment.populateType(eitherDataName.canonicalName(), eitherMeta);

        var originalNode = typeApplyNode(
                new Meta<Name>(Range.EMPTY, Nameless.INSTANCE),
                typeRefNode(
                        eitherMetaWithoutType,
                        idNode(Range.EMPTY, nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Kindchecker"),
                                "Either")),
                Lists.immutable.of(
                        typeRefNode(intMetaWithoutType, "Int"),
                        typeRefNode(stringMetaWithoutType, "String")));

        var expectedNode = typeApplyNode(
                new Meta<Attributes>(Range.EMPTY, new Attributes(Nameless.INSTANCE, TypeKind.INSTANCE)),
                typeRefNode(
                        eitherMeta,
                        idNode(Range.EMPTY, nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Kindchecker"),
                                "Either")),
                Lists.immutable.of(
                        typeRefNode(intMeta, "Int"),
                        typeRefNode(stringMeta, "String")));

        testSuccessfulKindcheck(environment, originalNode, expectedNode);
    }

    @Test
    void kindcheckIllKindedEitherTypeApply() {
        var applyRange = new Range(0, 1, 0, 1);

        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Kindchecker");
        var eitherName = new QualifiedName(nsName, "Either");
        var eitherDataName = new DataName(eitherName);
        var eitherKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE, TypeKind.INSTANCE);
        var eitherAttrs = new Attributes(eitherDataName, eitherKind);
        var eitherMeta = new Meta<Attributes>(Range.EMPTY, eitherAttrs);

        var eitherMetaWithoutType = eitherMeta.withMeta(eitherMeta.meta().name());

        var intAttrs = new Attributes(new BuiltInName("Int"), TypeKind.INSTANCE);
        var intMeta = new Meta<Attributes>(Range.EMPTY, intAttrs);

        var intMetaWithoutType = intMeta.withMeta(intMeta.meta().name());

        var environment = Environment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.populateType(eitherDataName.localName(), eitherMeta);
        environment.populateType(eitherDataName.canonicalName(), eitherMeta);

        var originalNode = typeApplyNode(
                new Meta<Name>(applyRange, Nameless.INSTANCE),
                typeRefNode(
                        eitherMetaWithoutType,
                        idNode(Range.EMPTY, nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Kindchecker"),
                                "Either")),
                Lists.immutable.of(
                        typeRefNode(intMetaWithoutType, "Int")));

        var collector = testFailedKindcheck(environment, originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                applyRange,
                "Mismatched type application! Expected: (*, *) -> *, Actual: A1 -> B1");
    }

    @Test
    void kindcheckIllKindedIntTypeApply() {
        var applyRange = new Range(0, 1, 0, 1);

        var intAttrs = new Attributes(new BuiltInName("Int"), TypeKind.INSTANCE);
        var intMeta = new Meta<Attributes>(Range.EMPTY, intAttrs);

        var intMetaWithoutType = intMeta.withMeta(intMeta.meta().name());

        var stringAttrs = new Attributes(new BuiltInName("String"), TypeKind.INSTANCE);
        var stringMeta = new Meta<Attributes>(Range.EMPTY, stringAttrs);

        var stringMetaWithoutType = stringMeta.withMeta(stringMeta.meta().name());

        var originalNode = typeApplyNode(
                new Meta<Name>(applyRange, Nameless.INSTANCE),
                typeRefNode(intMetaWithoutType, "Int"),
                Lists.immutable.of(typeRefNode(stringMetaWithoutType, "String")));

        var collector = testFailedKindcheck(Environment.withBuiltInTypes(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                applyRange,
                "Mismatched type application! Expected: *, Actual: A1 -> B1");
    }

    @Test
    void kindcheckFunType() {
        var intAttrs = new Attributes(new BuiltInName("Int"), TypeKind.INSTANCE);
        var intMeta = new Meta<Attributes>(Range.EMPTY, intAttrs);

        var intMetaWithoutType = intMeta.withMeta(intMeta.meta().name());

        var stringAttrs = new Attributes(new BuiltInName("String"), TypeKind.INSTANCE);
        var stringMeta = new Meta<Attributes>(Range.EMPTY, stringAttrs);

        var stringMetaWithoutType = stringMeta.withMeta(stringMeta.meta().name());

        var originalNode = funTypeNode(
                new Meta<Name>(Range.EMPTY, Nameless.INSTANCE),
                Lists.immutable.of(
                        typeRefNode(intMetaWithoutType, "Int"),
                        typeRefNode(stringMetaWithoutType, "String")),
                typeRefNode(stringMetaWithoutType, "String"));

        var expectedNode = funTypeNode(
                new Meta<Attributes>(Range.EMPTY, new Attributes(Nameless.INSTANCE, TypeKind.INSTANCE)),
                Lists.immutable.of(
                        typeRefNode(intMeta, "Int"),
                        typeRefNode(stringMeta, "String")),
                typeRefNode(stringMeta, "String"));

        testSuccessfulKindcheck(Environment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    void kindcheckIllKindedFunArgType() {
        var funArgRange = new Range(0, 1, 0, 1);

        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Kindchecker");
        var eitherName = new QualifiedName(nsName, "Either");
        var eitherDataName = new DataName(eitherName);
        var eitherKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE, TypeKind.INSTANCE);
        var eitherAttrs = new Attributes(eitherDataName, eitherKind);
        var eitherMeta = new Meta<Attributes>(funArgRange, eitherAttrs);

        var eitherMetaWithoutType = eitherMeta.withMeta(eitherMeta.meta().name());

        var intAttrs = new Attributes(new BuiltInName("Int"), TypeKind.INSTANCE);
        var intMeta = new Meta<Attributes>(Range.EMPTY, intAttrs);

        var intMetaWithoutType = intMeta.withMeta(intMeta.meta().name());

        var stringAttrs = new Attributes(new BuiltInName("String"), TypeKind.INSTANCE);
        var stringMeta = new Meta<Attributes>(Range.EMPTY, stringAttrs);

        var stringMetaWithoutType = stringMeta.withMeta(stringMeta.meta().name());

        var environment = Environment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.populateType(eitherDataName.localName(), eitherMeta);
        environment.populateType(eitherDataName.canonicalName(), eitherMeta);

        var originalNode = funTypeNode(
                new Meta<Name>(Range.EMPTY, Nameless.INSTANCE),
                Lists.immutable.of(
                        typeRefNode(intMetaWithoutType, "Int"),
                        typeRefNode(eitherMetaWithoutType, "Either")),
                typeRefNode(stringMetaWithoutType, "String"));

        var collector = testFailedKindcheck(environment, originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                funArgRange,
                "Mismatched kind! Expected: *, Actual: (*, *) -> *");
    }

    @Test
    void kindcheckIllKindedFunReturnType() {
        var funReturnRange = new Range(0, 1, 0, 1);

        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Kindchecker");
        var listName = new QualifiedName(nsName, "List");
        var listDataName = new DataName(listName);
        var listKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        var listAttrs = new Attributes(listDataName, listKind);
        var listMeta = new Meta<Attributes>(funReturnRange, listAttrs);

        var listMetaWithoutType = listMeta.withMeta(listMeta.meta().name());

        var intAttrs = new Attributes(new BuiltInName("Int"), TypeKind.INSTANCE);
        var intMeta = new Meta<Attributes>(Range.EMPTY, intAttrs);

        var intMetaWithoutType = intMeta.withMeta(intMeta.meta().name());

        var stringAttrs = new Attributes(new BuiltInName("String"), TypeKind.INSTANCE);
        var stringMeta = new Meta<Attributes>(Range.EMPTY, stringAttrs);

        var stringMetaWithoutType = stringMeta.withMeta(stringMeta.meta().name());

        var environment = Environment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.populateType(listDataName.localName(), listMeta);
        environment.populateType(listDataName.canonicalName(), listMeta);

        var originalNode = funTypeNode(
                new Meta<Name>(Range.EMPTY, Nameless.INSTANCE),
                Lists.immutable.of(
                        typeRefNode(intMetaWithoutType, "Int"),
                        typeRefNode(stringMetaWithoutType, "String")),
                typeRefNode(listMetaWithoutType, "List"));

        var collector = testFailedKindcheck(environment, originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                funReturnRange,
                "Mismatched kind! Expected: *, Actual: * -> *");
    }

    @Test
    void kindcheckTypeLambda() {
        var typeVarFName = new TypeVarName("F");
        var typeVarAName = new TypeVarName("A");

        // * -> *
        var typeVarFKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        // *
        var typeVarAKind = TypeKind.INSTANCE;

        // [* -> *, *] -> *
        var typeLambdaKind = new HigherKind(typeVarFKind, typeVarAKind, TypeKind.INSTANCE);

        var typeVarFMeta = Meta.of(new Attributes(typeVarFName, typeVarFKind));
        var typeVarAMeta = Meta.of(new Attributes(typeVarAName, typeVarAKind));

        var namelessMeta = Meta.of(new Attributes(Nameless.INSTANCE, TypeKind.INSTANCE));

        var typeLambdaMeta = Meta.of(new Attributes(Nameless.INSTANCE, typeLambdaKind));

        // [F, A] => F[A]
        var originalNode = typeLambdaNode(
                Meta.of(Nameless.INSTANCE),
                Lists.immutable.of(
                    forAllVarNode(Meta.of(typeVarFName), "F"),
                    forAllVarNode(Meta.of(typeVarAName), "A")),
                typeApplyNode(
                        Meta.of(Nameless.INSTANCE),
                        typeRefNode(Meta.of(typeVarFName), "F"),
                        Lists.immutable.of(
                                typeRefNode(Meta.of(typeVarAName), "A"))));

        var expectedNode = typeLambdaNode(
                typeLambdaMeta,
                Lists.immutable.of(
                    forAllVarNode(typeVarFMeta, "F"),
                    forAllVarNode(typeVarAMeta, "A")),
                typeApplyNode(
                        namelessMeta,
                        typeRefNode(typeVarFMeta, "F"),
                        Lists.immutable.of(typeRefNode(typeVarAMeta, "A"))));

        testSuccessfulKindcheck(Environment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    void kindcheckComplexTypeLambda() {
        var typeVarFName = new TypeVarName("F");
        var typeVarGName = new TypeVarName("G");
        var typeVarAName = new TypeVarName("A");

        // * -> *
        var typeVarFKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        // * -> *
        var typeVarGKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        // *
        var typeVarAKind = TypeKind.INSTANCE;

        // (* -> *, * -> *, *) -> *
        var typeLambdaKind = new HigherKind(typeVarFKind, typeVarGKind, typeVarAKind, TypeKind.INSTANCE);

        var typeVarFMeta = Meta.of(new Attributes(typeVarFName, typeVarFKind));
        var typeVarGMeta = Meta.of(new Attributes(typeVarGName, typeVarGKind));
        var typeVarAMeta = Meta.of(new Attributes(typeVarAName, typeVarAKind));

        var namelessMeta = Meta.of(new Attributes(Nameless.INSTANCE, TypeKind.INSTANCE));

        var typeLambdaMeta = Meta.of(new Attributes(Nameless.INSTANCE, typeLambdaKind));

        // [F, G, A] => F[G[A]]
        var originalNode = typeLambdaNode(
                new Meta<Name>(Range.EMPTY, Nameless.INSTANCE),
                Lists.immutable.of(
                        forAllVarNode(Meta.of(typeVarFName), "F"),
                        forAllVarNode(Meta.of(typeVarGName), "G"),
                        forAllVarNode(Meta.of(typeVarAName), "A")),
                typeApplyNode(
                        new Meta<Name>(Range.EMPTY, Nameless.INSTANCE),
                        typeRefNode(Meta.of(typeVarFName), "F"),
                        Lists.immutable.of(
                                typeApplyNode(
                                        new Meta<Name>(Range.EMPTY, Nameless.INSTANCE),
                                        typeRefNode(Meta.of(typeVarGName), "G"),
                                        Lists.immutable.of(typeRefNode(Meta.of(typeVarAName), "A"))))));

        var expectedNode = typeLambdaNode(
                typeLambdaMeta,
                Lists.immutable.of(
                        forAllVarNode(typeVarFMeta, "F"),
                        forAllVarNode(typeVarGMeta, "G"),
                        forAllVarNode(typeVarAMeta, "A")),
                typeApplyNode(
                        namelessMeta,
                        typeRefNode(typeVarFMeta, "F"),
                        Lists.immutable.of(
                                typeApplyNode(
                                        namelessMeta,
                                        typeRefNode(typeVarGMeta, "G"),
                                        Lists.immutable.of(typeRefNode(typeVarAMeta, "A"))))));

        testSuccessfulKindcheck(Environment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    void kindcheckTypeLambdaWithComplexFunction() {
        var typeVarFName = new TypeVarName("F");
        var typeVarGName = new TypeVarName("G");
        var typeVarAName = new TypeVarName("A");
        var typeVarBName = new TypeVarName("B");

        // * -> *
        var typeVarGKind = new HigherKind(Lists.immutable.of(TypeKind.INSTANCE), TypeKind.INSTANCE);
        // (* -> *, *) -> *
        var typeVarFKind = new HigherKind(Lists.immutable.of(typeVarGKind, TypeKind.INSTANCE), TypeKind.INSTANCE);
        // *
        var typeVarAKind = TypeKind.INSTANCE;
        // *
        var typeVarBKind = TypeKind.INSTANCE;

        // (* -> *, (* -> *, *) -> *, *, *) -> *
        var typeLambdaKind = new HigherKind(
                Lists.immutable.of(typeVarFKind, typeVarGKind, typeVarAKind, typeVarBKind),
                TypeKind.INSTANCE);

        var typeVarFMeta = Meta.of(new Attributes(typeVarFName, typeVarFKind));
        var typeVarGMeta = Meta.of(new Attributes(typeVarGName, typeVarGKind));
        var typeVarAMeta = Meta.of(new Attributes(typeVarAName, typeVarAKind));
        var typeVarBMeta = Meta.of(new Attributes(typeVarBName, typeVarBKind));

        var namelessMeta = Meta.of(new Attributes(Nameless.INSTANCE, TypeKind.INSTANCE));

        var typeLambdaMeta = Meta.of(new Attributes(Nameless.INSTANCE, typeLambdaKind));

        // [F, G, A, B] => F[G, A] -> F[G, B] -> G[B]
        var originalNode = typeLambdaNode(
                Meta.of(Nameless.INSTANCE),
                Lists.immutable.of(
                        forAllVarNode(Meta.of(typeVarFName), "F"),
                        forAllVarNode(Meta.of(typeVarGName), "G"),
                        forAllVarNode(Meta.of(typeVarAName), "A"),
                        forAllVarNode(Meta.of(typeVarBName), "B")),
                funTypeNode(
                        Meta.of(Nameless.INSTANCE),
                        Lists.immutable.of(
                                typeApplyNode(
                                        Meta.of(Nameless.INSTANCE),
                                        typeRefNode(Meta.of(typeVarFName), "F"),
                                        Lists.immutable.of(
                                                typeRefNode(Meta.of(typeVarGName), "G"),
                                                typeRefNode(Meta.of(typeVarAName), "A")))),
                        funTypeNode(
                                Meta.of(Nameless.INSTANCE),
                                Lists.immutable.of(
                                        typeApplyNode(
                                                Meta.of(Nameless.INSTANCE),
                                                typeRefNode(Meta.of(typeVarFName), "F"),
                                                Lists.immutable.of(
                                                        typeRefNode(Meta.of(typeVarGName), "G"),
                                                        typeRefNode(Meta.of(typeVarBName), "B")))),
                                typeApplyNode(
                                        Meta.of(Nameless.INSTANCE),
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
                        namelessMeta,
                        Lists.immutable.of(
                                typeApplyNode(
                                        namelessMeta,
                                        typeRefNode(typeVarFMeta, "F"),
                                        Lists.immutable.of(
                                                typeRefNode(typeVarGMeta, "G"),
                                                typeRefNode(typeVarAMeta, "A")))),
                        funTypeNode(
                                namelessMeta,
                                Lists.immutable.of(
                                        typeApplyNode(
                                                namelessMeta,
                                                typeRefNode(typeVarFMeta, "F"),
                                                Lists.immutable.of(
                                                        typeRefNode(typeVarGMeta, "G"),
                                                        typeRefNode(typeVarBMeta, "B")))),
                                typeApplyNode(
                                        namelessMeta,
                                        typeRefNode(typeVarGMeta, "G"),
                                        Lists.immutable.of(typeRefNode(typeVarBMeta, "B"))))));

        testSuccessfulKindcheck(Environment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    void kindCheckHigherTyCon() {
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Kindchecker");

        var functorName = new QualifiedName(nsName, "Functor");
        var functorDataName = new DataName(functorName);
        var functorKind = new HigherKind(new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE), TypeKind.INSTANCE);
        var functorAttrs = new Attributes(functorDataName, functorKind);
        var functorMeta = new Meta<Attributes>(Range.EMPTY, functorAttrs);

        var listName = new QualifiedName(nsName, "List");
        var listDataName = new DataName(listName);
        var listKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        var listAttrs = new Attributes(listDataName, listKind);
        var listMeta = new Meta<Attributes>(Range.EMPTY, listAttrs);

        var namelessMeta = Meta.of(new Attributes(Nameless.INSTANCE, TypeKind.INSTANCE));

        var environment = Environment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.populateType(listDataName.localName(), listMeta);
        environment.populateType(listDataName.canonicalName(), listMeta);
        environment.populateType(functorDataName.localName(), functorMeta);
        environment.populateType(functorDataName.canonicalName(), functorMeta);

        // Functor[List]
        var originalNode = typeApplyNode(
                Meta.of(Nameless.INSTANCE),
                typeRefNode(Meta.of(functorDataName), "Functor"),
                Lists.immutable.of(
                        typeRefNode(Meta.of(listDataName), "List")));

        var expectedNode = typeApplyNode(
                namelessMeta,
                typeRefNode(functorMeta, "Functor"),
                Lists.immutable.of(typeRefNode(listMeta, "List")));

        testSuccessfulKindcheck(environment, originalNode, expectedNode);
    }

    @Test
    void kindCheckEtaExpandHigherTyCon() {
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Kindchecker");

        var functorName = new QualifiedName(nsName, "Functor");
        var functorDataName = new DataName(functorName);
        var functorKind = new HigherKind(new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE), TypeKind.INSTANCE);
        var functorAttrs = new Attributes(functorDataName, functorKind);
        var functorMeta = new Meta<Attributes>(Range.EMPTY, functorAttrs);

        // * -> *
        var typeVarFKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE);
        var typeVarFName = new TypeVarName("F");
        var typeVarFMeta = Meta.of(new Attributes(typeVarFName, typeVarFKind));

        var namelessMeta = Meta.of(new Attributes(Nameless.INSTANCE, TypeKind.INSTANCE));

        var environment = Environment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.populateType(functorDataName.localName(), functorMeta);
        environment.populateType(functorDataName.canonicalName(), functorMeta);

        // F => Functor[F]
        var originalNode = typeLambdaNode(
                Meta.of(Nameless.INSTANCE),
                Lists.immutable.of(forAllVarNode(Meta.of(typeVarFName), "F")),
                typeApplyNode(
                        Meta.of(Nameless.INSTANCE),
                        typeRefNode(Meta.of(functorDataName), "Functor"),
                        Lists.immutable.of(
                                typeRefNode(Meta.of(typeVarFName), "F"))));

        var expectedNode = typeLambdaNode(
                Meta.of(new Attributes(Nameless.INSTANCE, functorKind)),
                Lists.immutable.of(forAllVarNode(typeVarFMeta, "F")),
                typeApplyNode(
                        namelessMeta,
                        typeRefNode(functorMeta, "Functor"),
                        Lists.immutable.of(typeRefNode(typeVarFMeta, "F"))));

        testSuccessfulKindcheck(environment, originalNode, expectedNode);
    }

    @Test
    void kindCheckHigherTyConTypeLambda() {
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Kindchecker");

        var functorName = new QualifiedName(nsName, "Functor");
        var functorDataName = new DataName(functorName);
        var functorKind = new HigherKind(new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE), TypeKind.INSTANCE);
        var functorAttrs = new Attributes(functorDataName, functorKind);
        var functorMeta = new Meta<Attributes>(Range.EMPTY, functorAttrs);

        // [* => *] => *
        var typeVarFKind = new HigherKind(functorKind, TypeKind.INSTANCE);
        var typeVarFName = new TypeVarName("F");
        var typeVarFMeta = Meta.of(new Attributes(typeVarFName, typeVarFKind));

        var namelessMeta = Meta.of(new Attributes(Nameless.INSTANCE, TypeKind.INSTANCE));

        var environment = Environment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.populateType(functorDataName.localName(), functorMeta);
        environment.populateType(functorDataName.canonicalName(), functorMeta);

        var typeLambdaKind = new HigherKind(typeVarFKind, TypeKind.INSTANCE);

        // F => F[Functor]
        var originalNode = typeLambdaNode(
                Meta.of(Nameless.INSTANCE),
                Lists.immutable.of(forAllVarNode(Meta.of(typeVarFName), "F")),
                typeApplyNode(
                        Meta.of(Nameless.INSTANCE),
                        typeRefNode(Meta.of(typeVarFName), "F"),
                        Lists.immutable.of(
                                typeRefNode(Meta.of(functorDataName), "Functor"))));

        var expectedNode = typeLambdaNode(
                Meta.of(new Attributes(Nameless.INSTANCE, typeLambdaKind)),
                Lists.immutable.of(forAllVarNode(typeVarFMeta, "F")),
                typeApplyNode(
                        namelessMeta,
                        typeRefNode(typeVarFMeta, "F"),
                        Lists.immutable.of(typeRefNode(functorMeta, "Functor"))));

        testSuccessfulKindcheck(environment, originalNode, expectedNode);
    }

    @Test
    void kindCheckBinaryTyConAdaptedToUnary() {
        var typeVarAName = new TypeVarName("A");
        var typeVarAKind = TypeKind.INSTANCE;
        var typeVarAMeta = Meta.of(new Attributes(typeVarAName, typeVarAKind));

        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Kindchecker");

        var eitherName = new QualifiedName(nsName, "Either");
        var eitherDataName = new DataName(eitherName);
        var eitherKind = new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE, TypeKind.INSTANCE);
        var eitherAttrs = new Attributes(eitherDataName, eitherKind);
        var eitherMeta = new Meta<Attributes>(Range.EMPTY, eitherAttrs);

        var functorName = new QualifiedName(nsName, "Functor");
        var functorDataName = new DataName(functorName);
        var functorKind = new HigherKind(new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE), TypeKind.INSTANCE);
        var functorAttrs = new Attributes(functorDataName, functorKind);
        var functorMeta = new Meta<Attributes>(Range.EMPTY, functorAttrs);

        var stringAttrs = new Attributes(new BuiltInName("String"), TypeKind.INSTANCE);
        var stringMeta = new Meta<Attributes>(Range.EMPTY, stringAttrs);

        var stringMetaWithoutType = stringMeta.withMeta(stringMeta.meta().name());

        var environment = Environment.withBuiltInTypes();
        environment.pushScope(new ImportedScope<>());
        environment.populateType(eitherDataName.localName(), eitherMeta);
        environment.populateType(eitherDataName.canonicalName(), eitherMeta);
        environment.populateType(functorDataName.localName(), functorMeta);
        environment.populateType(functorDataName.canonicalName(), functorMeta);

        var namelessMeta = Meta.of(new Attributes(Nameless.INSTANCE, TypeKind.INSTANCE));

        // Functor[A => Either[String, A]]
        var originalNode = typeApplyNode(
                Meta.of(Nameless.INSTANCE),
                typeRefNode(Meta.of(functorDataName), "Functor"),
                Lists.immutable.of(
                        typeLambdaNode(
                                Meta.of(Nameless.INSTANCE),
                                Lists.immutable.of(forAllVarNode(Meta.of(typeVarAName), "A")),
                                typeApplyNode(
                                        Meta.of(Nameless.INSTANCE),
                                        typeRefNode(Meta.of(eitherDataName), "Either"),
                                        Lists.immutable.of(
                                                typeRefNode(stringMetaWithoutType, "String"),
                                                typeRefNode(Meta.of(typeVarAName), "A"))))));

        var expectedNode = typeApplyNode(
                namelessMeta,
                typeRefNode(functorMeta, "Functor"),
                Lists.immutable.of(
                        typeLambdaNode(
                                Meta.of(new Attributes(Nameless.INSTANCE,
                                        new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE))),
                                Lists.immutable.of(forAllVarNode(typeVarAMeta, "A")),
                                typeApplyNode(
                                        namelessMeta,
                                        typeRefNode(eitherMeta, "Either"),
                                        Lists.immutable.of(
                                                typeRefNode(stringMeta, "String"),
                                                typeRefNode(typeVarAMeta, "A"))))));

        testSuccessfulKindcheck(environment, originalNode, expectedNode);
    }

    @Property
    void kindcheckBuiltInTypes(@ForAll("builtIns") Map.Entry<String, Meta<Attributes>> builtIn) {
        var originalRange = new Range(0, 1, 0, 1);

        var builtInName = builtIn.getKey();
        var builtInMeta = builtIn.getValue().withRange(originalRange);

        var metaWithoutType = builtInMeta.withMeta(builtInMeta.meta().name());

        var originalNode = typeRefNode(metaWithoutType, builtInName);

        var expectedNode = typeRefNode(builtInMeta, builtInName);

        testSuccessfulKindcheck(Environment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Provide
    Arbitrary<Map.Entry<String, Meta<Attributes>>> builtIns() {
        return Arbitraries.of(BuiltInScope.withBuiltInTypes().types().entrySet());
    }
}

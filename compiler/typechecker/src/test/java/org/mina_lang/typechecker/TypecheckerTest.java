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
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.DeclarationNode;
import org.mina_lang.syntax.ExprNode;
import org.mina_lang.syntax.NamespaceNode;
import org.mina_lang.syntax.TypeNode;

import net.jqwik.api.*;

public class TypecheckerTest {
    void testSuccessfulTypecheck(
            Environment<Attributes> environment,
            NamespaceNode<Name> originalNode,
            NamespaceNode<Attributes> expectedNode) {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, environment);
        var typecheckedNode = typechecker.typecheck(originalNode);
        assertThat(diagnostics.getDiagnostics(), is(empty()));
        assertThat(typecheckedNode, is(equalTo(expectedNode)));
    }

    void testSuccessfulTypecheck(
            Environment<Attributes> environment,
            DeclarationNode<Name> originalNode,
            DeclarationNode<Attributes> expectedNode) {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, environment);
        var typecheckedNode = typechecker.typecheck(originalNode);
        assertThat(diagnostics.getDiagnostics(), is(empty()));
        assertThat(typecheckedNode, is(equalTo(expectedNode)));
    }

    void testSuccessfulTypecheck(
            Environment<Attributes> environment,
            ExprNode<Name> originalNode,
            ExprNode<Attributes> expectedNode) {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, environment);
        var typecheckedNode = typechecker.typecheck(originalNode);
        assertThat(diagnostics.getDiagnostics(), is(empty()));
        assertThat(typecheckedNode, is(equalTo(expectedNode)));
    }

    void testSuccessfulTypecheck(
            Environment<Attributes> environment,
            TypeNode<Name> originalNode,
            TypeNode<Attributes> expectedNode) {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, environment);
        var typecheckedNode = typechecker.typecheck(originalNode);
        assertThat(diagnostics.getDiagnostics(), is(empty()));
        assertThat(typecheckedNode, is(equalTo(expectedNode)));
    }

    ErrorCollector testFailedTypecheck(
            Environment<Attributes> environment,
            ExprNode<Name> originalNode) {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, environment);
        typechecker.typecheck(originalNode);
        var errors = diagnostics.getErrors();
        assertThat("There should be type errors", errors, is(not(empty())));
        return diagnostics;
    }

    ErrorCollector testFailedTypecheck(
            Environment<Attributes> environment,
            TypeNode<Name> originalNode) {
        var diagnostics = new ErrorCollector();
        var typechecker = new Typechecker(diagnostics, environment);
        typechecker.typecheck(originalNode);
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
    void typecheckLiteralBoolean() {
        /* true */
        var originalNode = boolNode(Meta.<Name>of(Nameless.INSTANCE), true);
        var expectedNode = boolNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.BOOLEAN)), true);
        testSuccessfulTypecheck(Environment.empty(), originalNode, expectedNode);
    }

    @Test
    void typecheckLiteralChar() {
        /* 'c' */
        var originalNode = charNode(Meta.<Name>of(Nameless.INSTANCE), 'c');
        var expectedNode = charNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.CHAR)), 'c');
        testSuccessfulTypecheck(Environment.empty(), originalNode, expectedNode);
    }

    @Test
    void typecheckLiteralString() {
        /* "foo" */
        var originalNode = stringNode(Meta.<Name>of(Nameless.INSTANCE), "foo");
        var expectedNode = stringNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.STRING)), "foo");
        testSuccessfulTypecheck(Environment.empty(), originalNode, expectedNode);
    }

    @Test
    void typecheckLiteralInt() {
        /* 1 */
        var originalNode = intNode(Meta.<Name>of(Nameless.INSTANCE), 1);
        var expectedNode = intNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.INT)), 1);
        testSuccessfulTypecheck(Environment.empty(), originalNode, expectedNode);
    }

    @Test
    void typecheckLiteralLong() {
        /* 1L */
        var originalNode = longNode(Meta.<Name>of(Nameless.INSTANCE), 1L);
        var expectedNode = longNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.LONG)), 1L);
        testSuccessfulTypecheck(Environment.empty(), originalNode, expectedNode);
    }

    @Test
    void typecheckLiteralFloat() {
        /* 0.1F */
        var originalNode = floatNode(Meta.<Name>of(Nameless.INSTANCE), 0.1F);
        var expectedNode = floatNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.FLOAT)), 0.1F);
        testSuccessfulTypecheck(Environment.empty(), originalNode, expectedNode);
    }

    @Test
    void typecheckLiteralDouble() {
        /* 0.1 */
        var originalNode = doubleNode(Meta.<Name>of(Nameless.INSTANCE), 0.1);
        var expectedNode = doubleNode(Meta.of(new Attributes(Nameless.INSTANCE, Type.DOUBLE)), 0.1);
        testSuccessfulTypecheck(Environment.empty(), originalNode, expectedNode);
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

        testSuccessfulTypecheck(Environment.empty(), originalNode, expectedNode);
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

        var collector = testFailedTypecheck(Environment.empty(), originalNode);

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

        var collector = testFailedTypecheck(Environment.empty(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                elseRange,
                "Mismatched type! Expected: Int, Actual: String");
    }

    @Test
    void typecheckListTypeApply() {
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Typechecker");
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
                        idNode(Range.EMPTY, nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Typechecker"),
                                "List")),
                Lists.immutable.of(typeRefNode(intMetaWithoutType, "Int")));

        var expectedNode = typeApplyNode(
                new Meta<Attributes>(Range.EMPTY, new Attributes(Nameless.INSTANCE, TypeKind.INSTANCE)),
                typeRefNode(listMeta,
                        idNode(Range.EMPTY, nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Typechecker"),
                                "List")),
                Lists.immutable.of(typeRefNode(intMeta, "Int")));

        testSuccessfulTypecheck(environment, originalNode, expectedNode);
    }

    @Test
    void typecheckIllKindedListTypeApply() {
        var applyRange = new Range(0, 1, 0, 1);

        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Typechecker");
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
                        idNode(Range.EMPTY, nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Typechecker"),
                                "List")),
                Lists.immutable.of(
                        typeRefNode(intMetaWithoutType, "Int"),
                        typeRefNode(stringMetaWithoutType, "String")));

        var collector = testFailedTypecheck(environment, originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                applyRange,
                "Mismatched kind! Expected: * -> *, Actual: (*, *) -> *");
    }

    @Test
    void typecheckEitherTypeApply() {
        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Typechecker");
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
                        idNode(Range.EMPTY, nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Typechecker"),
                                "Either")),
                Lists.immutable.of(
                        typeRefNode(intMetaWithoutType, "Int"),
                        typeRefNode(stringMetaWithoutType, "String")));

        var expectedNode = typeApplyNode(
                new Meta<Attributes>(Range.EMPTY, new Attributes(Nameless.INSTANCE, TypeKind.INSTANCE)),
                typeRefNode(
                        eitherMeta,
                        idNode(Range.EMPTY, nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Typechecker"),
                                "Either")),
                Lists.immutable.of(
                        typeRefNode(intMeta, "Int"),
                        typeRefNode(stringMeta, "String")));

        testSuccessfulTypecheck(environment, originalNode, expectedNode);
    }

    @Test
    void typecheckIllKindedEitherTypeApply() {
        var applyRange = new Range(0, 1, 0, 1);

        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Typechecker");
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
                        idNode(Range.EMPTY, nsIdNode(Range.EMPTY, Lists.immutable.of("Mina", "Test"), "Typechecker"),
                                "Either")),
                Lists.immutable.of(
                        typeRefNode(intMetaWithoutType, "Int")));

        var collector = testFailedTypecheck(environment, originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                applyRange,
                "Mismatched kind! Expected: (*, *) -> *, Actual: * -> *");
    }

    @Test
    void typecheckIllKindedIntTypeApply() {
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

        var collector = testFailedTypecheck(Environment.withBuiltInTypes(), originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                applyRange,
                "Mismatched kind! Expected: *, Actual: * -> *");
    }

    @Test
    void typecheckFunType() {
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

        testSuccessfulTypecheck(Environment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Test
    void typecheckIllKindedFunArgType() {
        var funArgRange = new Range(0, 1, 0, 1);

        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Typechecker");
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

        var collector = testFailedTypecheck(environment, originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                funArgRange,
                "Mismatched kind! Expected: *, Actual: (*, *) -> *");
    }

    @Test
    void typecheckIllKindedFunReturnType() {
        var funReturnRange = new Range(0, 1, 0, 1);

        var nsName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Typechecker");
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

        var collector = testFailedTypecheck(environment, originalNode);

        assertDiagnostic(
                collector.getDiagnostics(),
                funReturnRange,
                "Mismatched kind! Expected: *, Actual: * -> *");
    }

    @Property
    void typecheckBuiltInTypes(@ForAll("builtIns") Map.Entry<String, Meta<Attributes>> builtIn) {
        var originalRange = new Range(0, 1, 0, 1);

        var builtInName = builtIn.getKey();
        var builtInMeta = builtIn.getValue().withRange(originalRange);

        var metaWithoutType = builtInMeta.withMeta(builtInMeta.meta().name());

        var originalNode = typeRefNode(metaWithoutType, builtInName);

        var expectedNode = typeRefNode(builtInMeta, builtInName);

        testSuccessfulTypecheck(Environment.withBuiltInTypes(), originalNode, expectedNode);
    }

    @Provide
    Arbitrary<Map.Entry<String, Meta<Attributes>>> builtIns() {
        return Arbitraries.of(BuiltInScope.withBuiltInTypes().types().entrySet());
    }
}

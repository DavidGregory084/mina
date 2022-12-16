package org.mina_lang.common.types;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.Test;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.common.names.QualifiedName;

import net.jqwik.api.*;

public class TypePrinterTest {
    TypePrinter printer = new TypePrinter();

    @Test
    void testUnsolvedTypePrinting() {
        // Starts with A1
        var typeVar0 = new UnsolvedType(0, TypeKind.INSTANCE);
        var doc0 = typeVar0.accept(printer);
        assertThat(doc0.render(80), is(equalTo("A1")));
        // Proceeds through alphabetic characters
        var typeVar1 = new UnsolvedType(1, TypeKind.INSTANCE);
        var doc1 = typeVar1.accept(printer);
        assertThat(doc1.render(80), is(equalTo("B1")));
        // After Z1, increments suffix
        var typeVar26 = new UnsolvedType(26, TypeKind.INSTANCE);
        var doc26 = typeVar26.accept(printer);
        assertThat(doc26.render(80), is(equalTo("A2")));
    }

    @Test
    void testTypeConstructorPrinting() {
        // TODO: Disambiguate names properly by accepting import environment in
        // TypePrinter constructor?
        var listDoc = new TypeConstructor(
                new QualifiedName(new NamespaceName(Lists.immutable.of("Mina", "Test"), "Printer"), "List"),
                new HigherKind(Lists.immutable.of(TypeKind.INSTANCE), TypeKind.INSTANCE)).accept(printer);
        assertThat(listDoc.render(80), is(equalTo("List")));
    }

    @Test
    void testTypeApplyPrinting() {
        var listTy = new TypeConstructor(
                new QualifiedName(new NamespaceName(Lists.immutable.of("Mina", "Test"), "Printer"), "List"),
                new HigherKind(Lists.immutable.of(TypeKind.INSTANCE), TypeKind.INSTANCE));

        var listIntDoc = new TypeApply(listTy, Lists.immutable.of(Type.INT), TypeKind.INSTANCE).accept(printer);

        assertThat(listIntDoc.render(80), is(equalTo("List[Int]")));
        assertThat(listIntDoc.render(4), is(equalTo(String.format("List[%n  Int%n]"))));

        var eitherTy = new TypeConstructor(
                new QualifiedName(new NamespaceName(Lists.immutable.of("Mina", "Test"), "Printer"), "Either"),
                new HigherKind(Lists.immutable.of(TypeKind.INSTANCE, TypeKind.INSTANCE), TypeKind.INSTANCE));

        var eitherStringIntDoc = new TypeApply(
                eitherTy,
                Lists.immutable.of(Type.STRING, Type.INT),
                TypeKind.INSTANCE).accept(printer);

        assertThat(eitherStringIntDoc.render(80), is(equalTo("Either[String, Int]")));
        assertThat(eitherStringIntDoc.render(4), is(equalTo(String.format("Either[%n  String,%n  Int%n]"))));
    }

    @Test
    void testFunctionTypePrinting() {
        var nullaryFunc = Type.function(Lists.immutable.empty(), Type.INT);
        assertThat(nullaryFunc.accept(printer).render(80), is(equalTo("() -> Int")));
        assertThat(nullaryFunc.accept(printer).render(4), is(equalTo(String.format("() ->%n  Int"))));

        var unaryFunc = Type.function(Lists.immutable.of(Type.INT), Type.INT);
        assertThat(unaryFunc.accept(printer).render(80), is(equalTo("Int -> Int")));
        assertThat(unaryFunc.accept(printer).render(4), is(equalTo(String.format("Int ->%n  Int"))));

        var nestedUnaryFunc = Type.function(
                Lists.immutable.of(Type.function(Lists.immutable.of(Type.INT), Type.INT)), Type.INT);
        assertThat(nestedUnaryFunc.accept(printer).render(80), is(equalTo("(Int -> Int) -> Int")));
        assertThat(nestedUnaryFunc.accept(printer).render(4),
                is(equalTo(String.format("(%n  Int ->%n    Int%n) ->%n  Int"))));

        var binaryFunc = Type.function(Lists.immutable.of(Type.INT, Type.INT), Type.INT);
        assertThat(binaryFunc.accept(printer).render(80), is(equalTo("(Int, Int) -> Int")));
        assertThat(binaryFunc.accept(printer).render(4), is(equalTo(String.format("(%n  Int,%n  Int%n) ->%n  Int"))));
    }

    @Test
    void testTypeLambdaPrinting() {
        var forAllVarA = new ForAllVar("A", TypeKind.INSTANCE);
        var forAllVarB = new ForAllVar("B", TypeKind.INSTANCE);
        var existsVarC = new ExistsVar("?C", TypeKind.INSTANCE);

        var unaryLam = new TypeLambda(Lists.immutable.of(forAllVarA), forAllVarA, TypeKind.INSTANCE);
        assertThat(unaryLam.accept(printer).render(80), is(equalTo("A => A")));
        assertThat(unaryLam.accept(printer).render(4), is(equalTo(String.format("A =>%n  A"))));

        var binaryLam = new TypeLambda(Lists.immutable.of(forAllVarA, forAllVarB), forAllVarA, TypeKind.INSTANCE);
        assertThat(binaryLam.accept(printer).render(80), is(equalTo("[A, B] => A")));
        assertThat(binaryLam.accept(printer).render(4), is(equalTo(String.format("[%n  A,%n  B%n] =>%n  A"))));

        var mixedLam = new TypeLambda(Lists.immutable.of(forAllVarA, existsVarC), forAllVarA, TypeKind.INSTANCE);
        assertThat(mixedLam.accept(printer).render(80), is(equalTo("[A, ?C] => A")));
        assertThat(mixedLam.accept(printer).render(4), is(equalTo(String.format("[%n  A,%n  ?C%n] =>%n  A"))));
    }

    @Property
    void forAllVarPrintsLiterally(@ForAll String tyVarName) {
        var typeVar = new ForAllVar(tyVarName, TypeKind.INSTANCE);
        var doc = typeVar.accept(printer);
        assertThat(doc.render(80), is(equalTo(tyVarName)));
    }

    @Property
    void existsVarPrintsLiterally(@ForAll String tyVarName) {
        var typeVar = new ExistsVar(tyVarName, TypeKind.INSTANCE);
        var doc = typeVar.accept(printer);
        assertThat(doc.render(80), is(equalTo(tyVarName)));
    }

    @Property
    void builtInTypePrintsLiterally(@ForAll("builtIns") BuiltInType builtIn) {
        var builtInDoc = builtIn.accept(printer);
        assertThat(builtInDoc.render(80), is(equalTo(builtIn.name())));
    }

    @Provide
    Arbitrary<BuiltInType> builtIns() {
        return Arbitraries.of(Type.builtIns.toSet());
    }
}

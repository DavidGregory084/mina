/*
 * SPDX-FileCopyrightText:  © 2026 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.optimiser;

import net.jqwik.api.*;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Multimaps;
import org.junit.jupiter.api.Test;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.TopLevelScope;
import org.mina_lang.common.names.*;
import org.mina_lang.common.operators.BinaryOp;
import org.mina_lang.common.operators.UnaryOp;
import org.mina_lang.common.types.*;
import org.mina_lang.ina.*;
import org.mina_lang.ina.Boolean;
import org.mina_lang.ina.Double;
import org.mina_lang.ina.Float;
import org.mina_lang.ina.Long;
import org.mina_lang.ina.String;
import org.mina_lang.optimiser.constants.*;

import java.util.ArrayDeque;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ConstantPropagationTest {
    // Specialisation -------------------------------

    // Conditional expressions
    @Test
    void usesConsequentWhenIfConditionLiteralTrue() {
        var propagation = new ConstantPropagation();

        // if true then "true" else "false"
        var result = propagation.optimiseExpression(
            new If(
                Type.STRING,
                new Boolean(true),
                new String("true"),
                new String("false")));

        // "true"
        assertThat(result, equalTo(new String("true")));
    }

    @Test
    void usesConsequentWhenIfConditionConstantTrue() {
        var varName = new LocalName("bool", 0);
        var propagation = new ConstantPropagation(Maps.mutable.of(varName, new Constant(new Boolean(true))));

        // if bool then "true" else "false"
        // bool known to be constant true
        var result = propagation.optimiseExpression(
            new If(
                Type.STRING,
                new Reference(varName, Type.BOOLEAN),
                new String("true"),
                new String("false")));

        // "true"
        assertThat(result, equalTo(new String("true")));
    }

    @Test
    void usesAlternativeWhenIfConditionLiteralFalse() {
        var propagation = new ConstantPropagation();

        // if false then "true" else "false"
        var result = propagation.optimiseExpression(
            new If(
                Type.STRING,
                new Boolean(false),
                new String("true"),
                new String("false")));

        // "false"
        assertThat(result, equalTo(new String("false")));
    }

    @Test
    void usesAlternativeWhenIfConditionConstantFalse() {
        var varName = new LocalName("bool", 0);
        var propagation = new ConstantPropagation(Maps.mutable.of(varName, new Constant(new Boolean(false))));

        // if bool then "true" else "false"
        // bool known to be constant false
        var result = propagation.optimiseExpression(
            new If(
                Type.STRING,
                new Reference(varName, Type.BOOLEAN),
                new String("true"),
                new String("false")));

        // "false"
        assertThat(result, equalTo(new String("false")));
    }

    @Test
    void retainsIfWhenConditionUnknown() {
        var varName = new LocalName("bool", 0);
        var propagation = new ConstantPropagation();

        // if bool then "true" else "false"
        // no known assignment for bool
        var ifNode = new If(
            Type.STRING,
            new Reference(varName, Type.BOOLEAN),
            new String("true"),
            new String("false"));

        var result = propagation.optimiseExpression(ifNode);

        // if bool then "true" else "false"
        assertThat(result, equalTo(ifNode));
    }

    @Test
    void retainsIfWhenConditionNonConstant() {
        var varName = new LocalName("bool", 0);
        var propagation = new ConstantPropagation(Maps.mutable.of(varName, NonConstant.VALUE));

        // if bool then "true" else "false"
        // bool known to be non-constant
        var ifNode = new If(
            Type.STRING,
            new Reference(varName, Type.BOOLEAN),
            new String("true"),
            new String("false"));

        var result = propagation.optimiseExpression(ifNode);

        // if bool then "true" else "false"
        assertThat(result, equalTo(ifNode));
    }

    @Test
    void usesMatchingCaseWhenScrutineeConstant() {
        var varName = new LocalName("bool", 0);
        var propagation = new ConstantPropagation(Maps.mutable.of(varName, new Constant(new Boolean(true))));

        // match bool with { case true -> false; case false -> true }
        // bool known to be constant true
        var result = propagation.optimiseExpression(
            new Match(
                Type.BOOLEAN,
                new Reference(varName, Type.BOOLEAN),
                Lists.immutable.of(
                    new Case(new LiteralPattern(new Boolean(true)), new Boolean(false)),
                    new Case(new LiteralPattern(new Boolean(false)), new Boolean(true)))));

        assertThat(result, equalTo(new Boolean(false)));
    }

    @Test
    void usesMatchingCaseWhenScrutineeKnownConstructor() {
        var varName = new LocalName("list", 0);
        var namespaceName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Constants");
        var listName = new DataName(new QualifiedName(namespaceName, "List"));
        var nilName = new ConstructorName(listName, new QualifiedName(namespaceName, "Nil"));
        var consName = new ConstructorName(listName, new QualifiedName(namespaceName, "Cons"));

        var listType = new TypeConstructor(listName.name(), new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE));

        var listIntType = new TypeApply(
            listType,
            Lists.immutable.of(Type.INT), TypeKind.INSTANCE);

        var tyVarA = new ForAllVar("A", TypeKind.INSTANCE);

        var nilType = new QuantifiedType(
            Lists.immutable.of(tyVarA),
            Type.function(new TypeApply(listType, Lists.immutable.of(tyVarA), TypeKind.INSTANCE)),
            TypeKind.INSTANCE);

        var propagation = new ConstantPropagation(Maps.mutable.of(varName, new ConstantConstructor(nilName, nilType)));

        // match list with { case Nil {} -> true; case Cons {} -> false }
        // list known to be Nil
        var matchNode = new Match(
            Type.BOOLEAN,
            new Reference(varName, Type.BOOLEAN),
            Lists.immutable.of(
                new Case(new ConstructorPattern(nilName, listIntType, Lists.immutable.empty()), new Boolean(true)),
                new Case(new ConstructorPattern(consName, listIntType, Lists.immutable.empty()), new Boolean(false))));

        var result = propagation.optimiseExpression(matchNode);

        assertThat(result, equalTo(matchNode.cases().getFirst().consequent()));
    }

    @Test
    void usesMatchingCasesWhenScrutineeKnownConstructorAndMultipleCasesMatch() {
        var varName = new LocalName("list", 0);
        var namespaceName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Constants");

        var listName = new DataName(new QualifiedName(namespaceName, "List"));
        var nilName = new ConstructorName(listName, new QualifiedName(namespaceName, "Nil"));
        var consName = new ConstructorName(listName, new QualifiedName(namespaceName, "Cons"));
        var tailName = new FieldName(consName, "tail");

        // List[Int]
        var listIntType = new TypeApply(
            new TypeConstructor(listName.name(), new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE)),
            Lists.immutable.of(Type.INT), TypeKind.INSTANCE);

        // Nil {}
        var nilPattern = new ConstructorPattern(nilName, listIntType, Lists.immutable.empty());
        // Cons {}
        var consPattern = new ConstructorPattern(consName, listIntType, Lists.immutable.empty());
        // Cons { tail: Nil {} }
        var consTailNilPattern = new ConstructorPattern(consName, listIntType, Lists.immutable.of(new FieldPattern(tailName, listIntType, nilPattern)));

        var propagation = new ConstantPropagation(Maps.mutable.of(varName, new KnownConstructor(consName)));

        // match list with { case Nil {} -> true; case Cons { tail: Nil {} } -> true; case Cons {} -> false }
        // list known to be Cons
        var matchNode = new Match(
            Type.BOOLEAN,
            new Reference(varName, listIntType),
            Lists.immutable.of(
                new Case(nilPattern, new Boolean(true)),
                new Case(consTailNilPattern, new Boolean(true)),
                new Case(consPattern, new Boolean(false))));

        var result = propagation.optimiseExpression(matchNode);

        // match list with { case Cons { tail: Nil {} } -> true; case Cons {} -> false }
        assertThat(result, equalTo(
            new Match(
                Type.BOOLEAN,
                new Reference(varName, listIntType),
                Lists.immutable.of(
                    new Case(consTailNilPattern, new Boolean(true)),
                    new Case(consPattern, new Boolean(false))))));
    }

    @Test
    void retainsMatchWhenScrutineeUnknown() {
        var varName = new LocalName("bool", 0);
        var propagation = new ConstantPropagation();

        // match bool with { case true -> false; case false -> true }
        // no known assignment for bool
        var matchNode = new Match(
            Type.BOOLEAN,
            new Reference(varName, Type.BOOLEAN),
            Lists.immutable.of(
                new Case(new LiteralPattern(new Boolean(true)), new Boolean(false)),
                new Case(new LiteralPattern(new Boolean(false)), new Boolean(true))));

        var result = propagation.optimiseExpression(matchNode);

        assertThat(result, equalTo(matchNode));
    }

    // Blocks

    // Lambda


    // Application
    @Test
    void usesConstantValueWhenFunctionKnownConstant() {
        var varName = new LocalName("constOne", 0);
        var propagation = new ConstantPropagation(Maps.mutable.of(varName, new Constant(new Int(1))));

        // constOne()
        // constOne known to produce constant 1
        var result = propagation.optimiseExpression(new Apply(
            Type.INT,
            new Reference(varName, Type.function(Type.INT)),
            Lists.immutable.empty()));

        // 1
        assertThat(result, equalTo(new Int(1)));
    }

    @Test
    void usesConstructorApplicationWhenFunctionConstantConstructor() {
        var varName = new LocalName("nil", 0);

        var namespaceName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Constants");
        var listName = new DataName(new QualifiedName(namespaceName, "List"));
        var nilName = new ConstructorName(listName, new QualifiedName(namespaceName, "Nil"));

        // List: * -> *
        var listType = new TypeConstructor(listName.name(), new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE));
        // A
        var tyVarA = new ForAllVar("A", TypeKind.INSTANCE);

        // [A] { () -> List[A] }
        var nilType = new QuantifiedType(
            Lists.immutable.of(tyVarA),
            Type.function(new TypeApply(listType, Lists.immutable.of(tyVarA), TypeKind.INSTANCE)),
            TypeKind.INSTANCE);

        var scope = new TopLevelScope<Attributes>(
            Maps.mutable.empty(),
            Maps.mutable.empty(),
            Maps.mutable.empty());

        scope.putValue(nilName.canonicalName(), Meta.of(nilName, nilType));

        var propagation = new ConstantPropagation(
            OptEnvironment.withScope(scope),
            Maps.mutable.of(varName, new ConstantConstructor(nilName, nilType)));

        // List[Int]
        var listIntType = new TypeApply(listType, Lists.immutable.of(Type.INT), TypeKind.INSTANCE);

        // nil()
        // nil known to produce constant Nil()
        var result = propagation.optimiseExpression(new Apply(
            listIntType,
            new Reference(varName, Type.function(listIntType)),
            Lists.immutable.empty()));

        // Nil()
        assertThat(result, equalTo(new Apply(
            listIntType,
            new Reference(nilName, nilType),
            Lists.immutable.empty())));
    }

    @Test
    void retainsFunctionOtherwiseOptimisingArguments() {

    }

    // Analysis -------------------------------------

    // Conditional expressions
    @Test
    void derivesUnknownForIfWhenCondUnknown() {
        var propagation = new ConstantPropagation();
        var varName = new LocalName("bool", 0);

        // if bool then "true" else "false"
        // no known assignment for bool
        var result = propagation.analyseExpression(
            new If(
                Type.STRING,
                new Reference(varName, Type.BOOLEAN),
                new String("true"),
                new String("false")));

        assertThat(result, equalTo(Unknown.VALUE));
    }

    @Test
    void derivesConstantForIfWhenCondLiteralAndConsequentConstant() {
        var propagation = new ConstantPropagation();

        // if true then "true" else "false"
        var result = propagation.analyseExpression(
            new If(
                Type.STRING,
                new Boolean(true),
                new String("true"),
                new String("false")));

        assertThat(result, equalTo(new Constant(new String("true"))));
    }

    @Test
    void derivesConstantForIfWhenCondLiteralAndAlternativeConstant() {
        var propagation = new ConstantPropagation();

        // if false then "true" else "false"
        var result = propagation.analyseExpression(
            new If(
                Type.STRING,
                new Boolean(false),
                new String("true"),
                new String("false")));

        assertThat(result, equalTo(new Constant(new String("false"))));
    }

    @Test
    void derivesConstantForIfWhenCondConstantAndConsequentConstant() {
        var varName = new LocalName("bool", 0);
        var propagation = new ConstantPropagation(Maps.mutable.of(varName, new Constant(new Boolean(true))));

        // if bool then "true" else "false"
        // bool known to be constant true
        var result = propagation.analyseExpression(
            new If(
                Type.STRING,
                new Reference(varName, Type.BOOLEAN),
                new String("true"),
                new String("false")));

        assertThat(result, equalTo(new Constant(new String("true"))));
    }

    @Test
    void derivesConstantForIfWhenCondConstantAndAlternativeConstant() {
        var varName = new LocalName("bool", 0);
        var propagation = new ConstantPropagation(Maps.mutable.of(varName, new Constant(new Boolean(false))));

        // if bool then "true" else "false"
        // bool known to be constant false
        var result = propagation.analyseExpression(
            new If(
                Type.STRING,
                new Reference(varName, Type.BOOLEAN),
                new String("true"),
                new String("false")));

        assertThat(result, equalTo(new Constant(new String("false"))));
    }

    @Test
    void derivesNonConstantForIfWhenCondNonConstantAndBranchesConflict() {
        var varName = new LocalName("bool", 0);
        var propagation = new ConstantPropagation(Maps.mutable.of(varName, NonConstant.VALUE));

        // if bool then "true" else "false"
        // bool known to be non-constant
        var result = propagation.analyseExpression(
            new If(
                Type.STRING,
                new Reference(varName, Type.BOOLEAN),
                new String("true"),
                new String("false")));

        assertThat(result, equalTo(NonConstant.VALUE));
    }

    @Test
    void derivesConstantForIfWhenCondNonConstantAndBranchesAreEqual() {
        var varName = new LocalName("bool", 0);
        var propagation = new ConstantPropagation(Maps.mutable.of(varName, NonConstant.VALUE));

        // if bool then "true" else "true"
        // bool known to be non-constant
        var result = propagation.analyseExpression(
            new If(
                Type.STRING,
                new Reference(varName, Type.BOOLEAN),
                new String("true"),
                new String("true")));

        assertThat(result, equalTo(new Constant(new String("true"))));
    }

    @Test
    void derivesUnknownForMatchWhenScrutineeUnknown() {
        var varName = new LocalName("bool", 0);
        var propagation = new ConstantPropagation();

        // match bool with { case true -> false; case false -> true }
        // no known assignment for bool
        var result = propagation.analyseExpression(
            new Match(
                Type.BOOLEAN,
                new Reference(varName, Type.BOOLEAN),
                Lists.immutable.of(
                    new Case(new LiteralPattern(new Boolean(true)), new Boolean(false)),
                    new Case(new LiteralPattern(new Boolean(false)), new Boolean(true)))));

        assertThat(result, equalTo(Unknown.VALUE));
    }

    @Test
    void derivesConstantForMatchingCaseWhenScrutineeConstant() {
        var varName = new LocalName("bool", 0);
        var propagation = new ConstantPropagation(Maps.mutable.of(varName, new Constant(new Boolean(true))));

        // match bool with { case true -> false; case false -> true }
        // no known assignment for bool
        var result = propagation.analyseExpression(
            new Match(
                Type.BOOLEAN,
                new Reference(varName, Type.BOOLEAN),
                Lists.immutable.of(
                    new Case(new LiteralPattern(new Boolean(true)), new Boolean(false)),
                    new Case(new LiteralPattern(new Boolean(false)), new Boolean(true)))));

        assertThat(result, equalTo(new Constant(new Boolean(false))));
    }

    @Test
    void derivesConstantForMatchingCaseWhenScrutineeKnownConstructor() {
        var varName = new LocalName("list", 0);
        var namespaceName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Constants");
        var listName = new DataName(new QualifiedName(namespaceName, "List"));
        var nilName = new ConstructorName(listName, new QualifiedName(namespaceName, "Nil"));
        var consName = new ConstructorName(listName, new QualifiedName(namespaceName, "Cons"));
        var listIntType = new TypeApply(
            new TypeConstructor(listName.name(), new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE)),
            Lists.immutable.of(Type.INT), TypeKind.INSTANCE);
        var propagation = new ConstantPropagation(Maps.mutable.of(varName, new KnownConstructor(nilName)));

        // match list with { case Nil {} -> true; case Cons {} -> false }
        // no known assignment for list
        var result = propagation.analyseExpression(
            new Match(
                Type.BOOLEAN,
                new Reference(varName, listIntType),
                Lists.immutable.of(
                    new Case(new ConstructorPattern(nilName, listIntType, Lists.immutable.empty()), new Boolean(true)),
                    new Case(new ConstructorPattern(consName, listIntType, Lists.immutable.empty()), new Boolean(false)))));

        assertThat(result, equalTo(new Constant(new Boolean(true))));
    }

    @Test
    void derivesConstantForConstantMatchCases() {
        var varName = new LocalName("bool", 0);
        var propagation = new ConstantPropagation(Maps.mutable.of(varName, NonConstant.VALUE));

        // match bool with { case true -> true; case false -> true }
        // bool known to be non-constant
        var result = propagation.analyseExpression(
            new Match(
                Type.BOOLEAN,
                new Reference(varName, Type.BOOLEAN),
                Lists.immutable.of(
                    new Case(new LiteralPattern(new Boolean(true)), new Boolean(true)),
                    new Case(new LiteralPattern(new Boolean(false)), new Boolean(true)))));

        assertThat(result, equalTo(new Constant(new Boolean(true))));
    }

    @Test
    void derivesNonConstantForNonConstantMatchCases() {
        var varName = new LocalName("bool", 0);
        var propagation = new ConstantPropagation(Maps.mutable.of(varName, NonConstant.VALUE));

        // match bool with { case true -> false; case false -> true }
        // bool known to be non-constant
        var result = propagation.analyseExpression(
            new Match(
                Type.BOOLEAN,
                new Reference(varName, Type.BOOLEAN),
                Lists.immutable.of(
                    new Case(new LiteralPattern(new Boolean(true)), new Boolean(false)),
                    new Case(new LiteralPattern(new Boolean(false)), new Boolean(true)))));

        assertThat(result, equalTo(NonConstant.VALUE));
    }

    // Lambda
    @Test
    void derivesNonConstantForLambdaAndParams() {
        var firstParam = new LocalName("x", 1);
        var secondParam = new LocalName("y", 2);
        var propagation = new ConstantPropagation();

        // (x: Int, y: Int) -> x
        var result = propagation.analyseExpression(
            new Lambda(
                Type.INT,
                Lists.immutable.of(new Param(firstParam, Type.INT), new Param(secondParam, Type.INT)),
                new Reference(firstParam, Type.INT)));

        assertThat(result, equalTo(NonConstant.VALUE));
        assertThat(propagation.getEnvironment().get(firstParam), equalTo(NonConstant.VALUE));
        assertThat(propagation.getEnvironment().get(secondParam), equalTo(NonConstant.VALUE));
    }

    // Application
    @Test
    void derivesConstantForApplicationOfConstantFunc() {
        var funName = new LocalName("one", 0);
        var funTy = Type.function(Type.INT);
        var constant = new Constant(new Int(1));

        var propagation = new ConstantPropagation(Maps.mutable.of(funName, constant));

        // one()
        // one known to be constant 1
        var result = propagation.analyseExpression(
            new Apply(
                Type.INT,
                new Reference(funName, funTy),
                Lists.immutable.empty()));

        assertThat(result, equalTo(constant));
    }

    @Test
    void derivesConstantForRedexWithConstantArg() {
        var funTy = Type.function(Type.INT, Type.INT);
        var paramName = new LocalName("x", 1);
        var constantArg = new Constant(new Int(1));
        var constantResult = new Constant(new Int(2));

        var propagation = new ConstantPropagation();

        // (x -> x + 1)(1)
        var result = propagation.analyseExpression(
            new Apply(
                Type.INT,
                new Lambda(
                    funTy,
                    Lists.immutable.of(new Param(paramName, Type.INT)),
                    new BinOp(Type.INT, new Reference(paramName, Type.INT), BinaryOp.ADD, new Int(1))),
                Lists.immutable.of(new Int(1))));

        assertThat(result, equalTo(constantResult));
        assertThat(propagation.getEnvironment().get(paramName), equalTo(constantArg));
    }

    @Test
    void derivesNonConstantForRedexWithNonConstantArg() {
        var funTy = Type.function(Type.INT, Type.INT);
        var paramName = new LocalName("x", 1);
        var varName = new LocalName("num", 0);

        var propagation = new ConstantPropagation(Maps.mutable.of(varName, NonConstant.VALUE));

        // (x -> x + 1)(num)
        var result = propagation.analyseExpression(
            new Apply(
                Type.INT,
                new Lambda(
                    funTy,
                    Lists.immutable.of(new Param(paramName, Type.INT)),
                    new BinOp(Type.INT, new Reference(paramName, Type.INT), BinaryOp.ADD, new Int(1))),
                Lists.immutable.of(new Reference(varName, Type.INT))));

        assertThat(result, equalTo(NonConstant.VALUE));
        assertThat(propagation.getEnvironment().get(paramName), equalTo(NonConstant.VALUE));
    }

    @Test
    void doesntAddUnknownFunctionToWorklist() {
        var funName = new LocalName("const", 0);
        var xParam = new LocalName("x", 1);
        var yParam = new LocalName("y", 2);
        var funTy = Type.function(Type.INT, Type.INT, Type.INT);

        var worklist = new ArrayDeque<Named>();
        var environment = Maps.mutable.<Named, Result>empty();
        var funParams = Maps.mutable.<Named, ImmutableList<Param>>of(
            funName, Lists.immutable.of(
                new Param(xParam, Type.INT),
                new Param(yParam, Type.INT)));

        var propagation = new ConstantPropagation(
            OptEnvironment.empty(),
            environment,
            worklist,
            new HashMap<>(), // the body of this function is unknown
            Multimaps.mutable.set.empty(),
            funParams
        );

        var constOne = new Constant(new Int(1));
        var constTwo = new Constant(new Int(2));

        // const(1, 2)
        // no known assignment for arguments x and y
        var result = propagation.analyseExpression(
            new Apply(
                Type.INT,
                new Reference(funName, funTy),
                Lists.immutable.of(new Int(1), new Int(2))));

        assertThat(result, equalTo(Unknown.VALUE)); // We don't know about the function body yet
        assertThat(propagation.getEnvironment().get(xParam), equalTo(constOne));
        assertThat(propagation.getEnvironment().get(yParam), equalTo(constTwo));
        assertThat(worklist, is(empty()));
    }

    @Test
    void addsKnownFunctionToWorklist() {
        var namespaceName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Constants");
        var constQn = new QualifiedName(namespaceName, "const");
        var constName = new LetName(constQn);
        var xParam = new LocalName("x", 1);
        var yParam = new LocalName("y", 2);
        var funTy = Type.function(Type.INT, Type.INT, Type.INT);
        var constBody = new Lambda(
            funTy,
            Lists.immutable.of(new Param(xParam, Type.INT), new Param(yParam, Type.INT)),
            new Reference(xParam, Type.INT));

        var worklist = new ArrayDeque<Named>();
        var environment = Maps.mutable.<Named, Result>empty();
        var letBodies = Maps.mutable.<Named, Expression>of(constName, constBody); // We know the definition of "const"
        var funParams = Maps.mutable.<Named, ImmutableList<Param>>of(
            constName, Lists.immutable.of(
                new Param(xParam, Type.INT),
                new Param(yParam, Type.INT)));

        var propagation = new ConstantPropagation(
            OptEnvironment.empty(),
            environment,
            worklist,
            letBodies,
            Multimaps.mutable.set.empty(),
            funParams
        );

        var constOne = new Constant(new Int(1));
        var constTwo = new Constant(new Int(2));

        // const(1, 2)
        // no known assignment for arguments x and y
        var result = propagation.analyseExpression(
            new Apply(
                Type.INT,
                new Reference(constName, funTy),
                Lists.immutable.of(new Int(1), new Int(2))));

        assertThat(result, equalTo(Unknown.VALUE)); // We don't know about the function body yet
        assertThat(propagation.getEnvironment().get(xParam), equalTo(constOne));
        assertThat(propagation.getEnvironment().get(yParam), equalTo(constTwo));
        assertThat(worklist, contains(constName)); // The body of the function is queued for processing
    }

    @Test
    void derivesNonConstantResultForTopLevelLetCalledWithConstantArgs() {
        var namespaceName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Constants");
        var constQn = new QualifiedName(namespaceName, "const");
        var constName = new LetName(constQn);
        var oneQn = new QualifiedName(namespaceName, "one");
        var oneName = new LetName(oneQn);

        var xParam = new LocalName("x", 1);
        var yParam = new LocalName("y", 2);
        var funTy = Type.function(Type.INT, Type.INT, Type.INT);

        var propagation = new ConstantPropagation();

        propagation.analyseDeclarations(Lists.immutable.of(
            // let const = (x: Int, y: Int) -> x
            new Let(constName, funTy, new Lambda(
                Type.INT,
                Lists.immutable.of(new Param(xParam, Type.INT), new Param(yParam, Type.INT)),
                new Reference(xParam, Type.INT))),
            // let one = const(1, 2)
            new Let(oneName, Type.INT, new Apply(
                Type.INT,
                new Reference(constName, Type.INT),
                Lists.immutable.of(new Int(1), new Int(2))))
        ));

        assertThat(propagation.getEnvironment().get(constName), equalTo(NonConstant.VALUE));
        assertThat(propagation.getEnvironment().get(xParam), equalTo(NonConstant.VALUE));
        assertThat(propagation.getEnvironment().get(yParam), equalTo(NonConstant.VALUE));
    }

    @Test
    void derivesNonConstantResultForTopLevelLetCalledWithConflictingConstantArgs() {
        var namespaceName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Constants");
        var constQn = new QualifiedName(namespaceName, "const");
        var constName = new LetName(constQn);
        var oneQn = new QualifiedName(namespaceName, "one");
        var oneName = new LetName(oneQn);
        var twoQn = new QualifiedName(namespaceName, "two");
        var twoName = new LetName(twoQn);

        var xParam = new LocalName("x", 1);
        var yParam = new LocalName("y", 2);
        var funTy = Type.function(Type.INT, Type.INT, Type.INT);

        var propagation = new ConstantPropagation();

        propagation.analyseDeclarations(Lists.immutable.of(
            // let const = (x: Int, y: Int) -> x
            new Let(constName, funTy, new Lambda(
                Type.INT,
                Lists.immutable.of(new Param(xParam, Type.INT), new Param(yParam, Type.INT)),
                new Reference(xParam, Type.INT))),
            // let one = const(1, 2)
            new Let(oneName, Type.INT, new Apply(
                Type.INT,
                new Reference(constName, Type.INT),
                Lists.immutable.of(new Int(1), new Int(2)))),
            // let two = const(2, 3)
            new Let(twoName, Type.INT, new Apply(
                Type.INT,
                new Reference(constName, Type.INT),
                Lists.immutable.of(new Int(2), new Int(3))))
        ));

        assertThat(propagation.getEnvironment().get(constName), equalTo(NonConstant.VALUE));
        assertThat(propagation.getEnvironment().get(xParam), equalTo(NonConstant.VALUE));
        assertThat(propagation.getEnvironment().get(yParam), equalTo(NonConstant.VALUE));
    }

    // Blocks
    @Test
    void derivesConstantResultForLocalLetCalledWithConstantArgs() {
        var constName = new LocalName("const", 0);
        var oneName = new LocalName("one", 1);

        var xParam = new LocalName("x", 1);
        var yParam = new LocalName("y", 2);
        var funTy = Type.function(Type.INT, Type.INT, Type.INT);

        var propagation = new ConstantPropagation();
        var constOne = new Constant(new Int(1));
        var constTwo = new Constant(new Int(2));

        var result = propagation.analyseExpression(new Block(
            Type.INT,
            // {
            Lists.immutable.of(
                // let const = (x: Int, y: Int) -> x
                new LetAssign(constName, funTy, new Lambda(
                    Type.INT,
                    Lists.immutable.of(new Param(xParam, Type.INT), new Param(yParam, Type.INT)),
                    new Reference(xParam, Type.INT))),
                // let one = const(1, 2)
                new LetAssign(oneName, Type.INT, new Apply(
                    Type.INT,
                    new Reference(constName, Type.INT),
                    Lists.immutable.of(new Int(1), new Int(2))))),
            // one + 1
            new BinOp(Type.INT, new Reference(oneName, Type.INT), BinaryOp.ADD, new Int(1))
            // }
        ));

        assertThat(result, equalTo(constTwo));
        assertThat(propagation.getEnvironment().get(constName), equalTo(constOne));
        assertThat(propagation.getEnvironment().get(xParam), equalTo(constOne));
        assertThat(propagation.getEnvironment().get(yParam), equalTo(constTwo));
    }

    @Test
    void derivesNonConstantResultForLocalLetCalledWithConflictingConstantArgs() {
        var constName = new LocalName("const", 0);
        var oneName = new LocalName("one", 1);
        var twoName = new LocalName("two", 2);

        var xParam = new LocalName("x", 1);
        var yParam = new LocalName("y", 2);
        var funTy = Type.function(Type.INT, Type.INT, Type.INT);

        var propagation = new ConstantPropagation();

        propagation.analyseExpression(new Block(
            Type.INT,
            // {
            Lists.immutable.of(
                // let const = (x: Int, y: Int) -> x
                new LetAssign(constName, funTy, new Lambda(
                    Type.INT,
                    Lists.immutable.of(new Param(xParam, Type.INT), new Param(yParam, Type.INT)),
                    new Reference(xParam, Type.INT))),
                // let one = const(1, 2)
                new LetAssign(oneName, Type.INT, new Apply(
                    Type.INT,
                    new Reference(constName, Type.INT),
                    Lists.immutable.of(new Int(1), new Int(2)))),
                // let two = const(2, 3)
                new LetAssign(twoName, Type.INT, new Apply(
                    Type.INT,
                    new Reference(constName, Type.INT),
                    Lists.immutable.of(new Int(2), new Int(3))))),
            // two + 1
            new BinOp(Type.INT, new Reference(twoName, Type.INT), BinaryOp.ADD, new Int(1))
            // }
        ));

        assertThat(propagation.getEnvironment().get(constName), equalTo(NonConstant.VALUE));
        assertThat(propagation.getEnvironment().get(xParam), equalTo(NonConstant.VALUE));
        assertThat(propagation.getEnvironment().get(yParam), equalTo(NonConstant.VALUE));
    }

    // Boolean not
    @Test
    void derivesConstantForBooleanNotOfConstants() {
        var propagation = new ConstantPropagation();

        // !false
        var result = propagation.analyseExpression(
            new UnOp(Type.INT, UnaryOp.BOOLEAN_NOT, new Boolean(false)));

        assertThat(result, equalTo(new Constant(new Boolean(true))));
    }

    // Bitwise not
    @Test
    void derivesConstantForBitwiseNotOfConstantInt() {
        var propagation = new ConstantPropagation();

        // ~16
        var result = propagation.analyseExpression(
            new UnOp(Type.INT, UnaryOp.BITWISE_NOT, new Int(16)));

        assertThat(result, equalTo(new Constant(new Int(-17))));
    }

    @Test
    void derivesConstantForBitwiseNotOfConstantLong() {
        var propagation = new ConstantPropagation();

        // ~16L
        var result = propagation.analyseExpression(
            new UnOp(Type.LONG, UnaryOp.BITWISE_NOT, new Long(16L)));

        assertThat(result, equalTo(new Constant(new Long(-17L))));
    }

    // Negation
    @Test
    void derivesConstantForNegationOfConstantInt() {
        var propagation = new ConstantPropagation();

        // -2
        var result = propagation.analyseExpression(
            new UnOp(Type.INT, UnaryOp.NEGATE, new Int(2)));

        assertThat(result, equalTo(new Constant(new Int(-2))));
    }

    @Test
    void derivesConstantForNegationOfConstantLong() {
        var propagation = new ConstantPropagation();

        // -2
        var result = propagation.analyseExpression(
            new UnOp(Type.LONG, UnaryOp.NEGATE, new Long(2L)));

        assertThat(result, equalTo(new Constant(new Long(-2L))));
    }

    @Test
    void derivesConstantForNegationOfConstantFloat() {
        var propagation = new ConstantPropagation();

        // -2.0F
        var result = propagation.analyseExpression(
            new UnOp(Type.FLOAT, UnaryOp.NEGATE, new Float(2.0F)));

        assertThat(result, equalTo(new Constant(new Float(-2.0F))));
    }

    @Test
    void derivesConstantForNegationOfConstantDouble() {
        var propagation = new ConstantPropagation();

        // -2.0
        var result = propagation.analyseExpression(
            new UnOp(Type.DOUBLE, UnaryOp.NEGATE, new Double(2.0)));

        assertThat(result, equalTo(new Constant(new Double(-2.0))));
    }

    // Addition
    @Test
    void derivesConstantForAdditionOfConstantInt() {
        var propagation = new ConstantPropagation();

        // 2 + 2
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(2), BinaryOp.ADD, new Int(2)));

        assertThat(result, equalTo(new Constant(new Int(4))));
    }

    @Test
    void derivesConstantForAdditionOfConstantLong() {
        var propagation = new ConstantPropagation();

        // 2L + 2L
        var result = propagation.analyseExpression(
            new BinOp(Type.LONG, new Long(2L), BinaryOp.ADD, new Long(2L)));

        assertThat(result, equalTo(new Constant(new Long(4L))));
    }

    @Test
    void derivesConstantForAdditionOfConstantFloat() {
        var propagation = new ConstantPropagation();

        // 2.0F + 2.0F
        var result = propagation.analyseExpression(
            new BinOp(Type.FLOAT, new Float(2.0F), BinaryOp.ADD, new Float(2.0F)));

        assertThat(result, equalTo(new Constant(new Float(4.0F))));
    }

    @Test
    void derivesConstantForAdditionOfConstantDouble() {
        var propagation = new ConstantPropagation();

        // 2.0 + 2.0
        var result = propagation.analyseExpression(
            new BinOp(Type.DOUBLE, new Double(2.0), BinaryOp.ADD, new Double(2.0)));

        assertThat(result, equalTo(new Constant(new Double(4.0))));
    }

    // Subtraction
    @Test
    void derivesConstantForSubtractionOfConstantInt() {
        var propagation = new ConstantPropagation();

        // 4 - 2
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(4), BinaryOp.SUBTRACT, new Int(2)));

        assertThat(result, equalTo(new Constant(new Int(2))));
    }

    @Test
    void derivesConstantForSubtractionOfConstantLong() {
        var propagation = new ConstantPropagation();

        // 4L - 2L
        var result = propagation.analyseExpression(
            new BinOp(Type.LONG, new Long(4), BinaryOp.SUBTRACT, new Long(2)));

        assertThat(result, equalTo(new Constant(new Long(2))));
    }

    @Test
    void derivesConstantForSubtractionOfConstantFloat() {
        var propagation = new ConstantPropagation();

        // 4.0F - 2.0F
        var result = propagation.analyseExpression(
            new BinOp(Type.FLOAT, new Float(4.0F), BinaryOp.SUBTRACT, new Float(2.0F)));

        assertThat(result, equalTo(new Constant(new Float(2.0F))));
    }

    @Test
    void derivesConstantForSubtractionOfConstantDouble() {
        var propagation = new ConstantPropagation();

        // 4.0 - 2.0
        var result = propagation.analyseExpression(
            new BinOp(Type.DOUBLE, new Double(4.0), BinaryOp.SUBTRACT, new Double(2.0)));

        assertThat(result, equalTo(new Constant(new Double(2.0))));
    }

    // Multiplication
    @Test
    void derivesConstantForMultiplicationOfConstantInt() {
        var propagation = new ConstantPropagation();

        // 4 * 2
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(4), BinaryOp.MULTIPLY, new Int(2)));

        assertThat(result, equalTo(new Constant(new Int(8))));
    }

    @Test
    void derivesConstantForMultiplicationOfConstantLong() {
        var propagation = new ConstantPropagation();

        // 4L * 2L
        var result = propagation.analyseExpression(
            new BinOp(Type.LONG, new Long(4L), BinaryOp.MULTIPLY, new Long(2L)));

        assertThat(result, equalTo(new Constant(new Long(8L))));
    }

    @Test
    void derivesConstantForMultiplicationOfConstantFloat() {
        var propagation = new ConstantPropagation();

        // 4.0F * 2.0F
        var result = propagation.analyseExpression(
            new BinOp(Type.FLOAT, new Float(4.0F), BinaryOp.MULTIPLY, new Float(2.0F)));

        assertThat(result, equalTo(new Constant(new Float(8.0F))));
    }

    @Test
    void derivesConstantForMultiplicationOfConstantDouble() {
        var propagation = new ConstantPropagation();

        // 4.0 * 2.0
        var result = propagation.analyseExpression(
            new BinOp(Type.DOUBLE, new Double(4.0), BinaryOp.MULTIPLY, new Double(2.0)));

        assertThat(result, equalTo(new Constant(new Double(8.0))));
    }

    // Division
    @Test
    void derivesConstantForDivisionOfConstantInt() {
        var propagation = new ConstantPropagation();

        // 8 / 2
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(8), BinaryOp.DIVIDE, new Int(2)));

        assertThat(result, equalTo(new Constant(new Int(4))));
    }

    @Test
    void derivesConstantForDivisionOfConstantLong() {
        var propagation = new ConstantPropagation();

        // 8L / 2L
        var result = propagation.analyseExpression(
            new BinOp(Type.LONG, new Long(8L), BinaryOp.DIVIDE, new Long(2L)));

        assertThat(result, equalTo(new Constant(new Long(4L))));
    }

    @Test
    void derivesConstantForDivisionOfConstantFloat() {
        var propagation = new ConstantPropagation();

        // 8.0F / 2.0F
        var result = propagation.analyseExpression(
            new BinOp(Type.FLOAT, new Float(8.0F), BinaryOp.DIVIDE, new Float(2.0F)));

        assertThat(result, equalTo(new Constant(new Float(4.0F))));
    }

    @Test
    void derivesConstantForDivisionOfConstantDouble() {
        var propagation = new ConstantPropagation();

        // 8.0 / 2.0
        var result = propagation.analyseExpression(
            new BinOp(Type.DOUBLE, new Double(8.0), BinaryOp.DIVIDE, new Double(2.0)));

        assertThat(result, equalTo(new Constant(new Double(4.0))));
    }

    @Test
    void derivesUnknownForIntDivisionByZero() {
        var propagation = new ConstantPropagation();

        // 8 / 0
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(8), BinaryOp.DIVIDE, new Int(0)));

        assertThat(result, equalTo(Unknown.VALUE));
    }

    // Modulus
    @Test
    void derivesConstantForModulusOfIntConstant() {
        var propagation = new ConstantPropagation();

        // 9 % 2
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(9), BinaryOp.MODULUS, new Int(2)));

        assertThat(result, equalTo(new Constant(new Int(1))));
    }

    @Test
    void derivesConstantForModulusOfLongConstant() {
        var propagation = new ConstantPropagation();

        // 9L % 2L
        var result = propagation.analyseExpression(
            new BinOp(Type.LONG, new Long(9L), BinaryOp.MODULUS, new Long(2L)));

        assertThat(result, equalTo(new Constant(new Long(1))));
    }

    @Test
    void derivesConstantForModulusOfFloatConstant() {
        var propagation = new ConstantPropagation();

        // 9.0F % 2.0F
        var result = propagation.analyseExpression(
            new BinOp(Type.FLOAT, new Float(9.0F), BinaryOp.MODULUS, new Float(2.0F)));

        assertThat(result, equalTo(new Constant(new Float(1.0F))));
    }

    @Test
    void derivesConstantForModulusOfDoubleConstant() {
        var propagation = new ConstantPropagation();

        // 9.0 % 2.0
        var result = propagation.analyseExpression(
            new BinOp(Type.DOUBLE, new Double(9.0), BinaryOp.MODULUS, new Double(2.0)));

        assertThat(result, equalTo(new Constant(new Double(1.0))));
    }

    @Test
    void derivesUnknownForIntegerModulusByZero() {
        var propagation = new ConstantPropagation();

        // 9 % 0
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(9), BinaryOp.MODULUS, new Int(0)));

        assertThat(result, equalTo(Unknown.VALUE));
    }

    // Left shift
    @Test
    void derivesConstantForLeftShiftOfIntConstantByInt() {
        var propagation = new ConstantPropagation();

        // 9 << 2
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(9), BinaryOp.SHIFT_LEFT, new Int(2)));

        assertThat(result, equalTo(new Constant(new Int(36))));
    }

    @Test
    void derivesConstantForLeftShiftOfLongConstantByInt() {
        var propagation = new ConstantPropagation();

        // 9L << 2
        var result = propagation.analyseExpression(
            new BinOp(Type.LONG, new Long(9L), BinaryOp.SHIFT_LEFT, new Int(2)));

        assertThat(result, equalTo(new Constant(new Long(36L))));
    }

    // Right shift
    @Test
    void derivesConstantForRightShiftOfIntConstantByInt() {
        var propagation = new ConstantPropagation();

        // 36 >> 1
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(36), BinaryOp.SHIFT_RIGHT, new Int(1)));

        assertThat(result, equalTo(new Constant(new Int(18))));
    }

    @Test
    void derivesConstantForRightShiftOfLongConstantByInt() {
        var propagation = new ConstantPropagation();

        // 36L >> 1
        var result = propagation.analyseExpression(
            new BinOp(Type.LONG, new Long(36L), BinaryOp.SHIFT_RIGHT, new Int(1)));

        assertThat(result, equalTo(new Constant(new Long(18L))));
    }

    // Unsigned right shift
    @Test
    void derivesConstantForUnsignedRightShiftOfIntConstantByInt() {
        var propagation = new ConstantPropagation();

        // 36 >>> 1
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(36), BinaryOp.UNSIGNED_SHIFT_RIGHT, new Int(1)));

        assertThat(result, equalTo(new Constant(new Int(18))));
    }

    @Test
    void derivesConstantForUnsignedRightShiftOfLongConstantByInt() {
        var propagation = new ConstantPropagation();

        // 36L >>> 1
        var result = propagation.analyseExpression(
            new BinOp(Type.LONG, new Long(36L), BinaryOp.UNSIGNED_SHIFT_RIGHT, new Int(1)));

        assertThat(result, equalTo(new Constant(new Long(18L))));
    }

    // Bitwise and
    @Test
    void derivesConstantForBitwiseAndOfConstantInt() {
        var propagation = new ConstantPropagation();

        // 37 & 1
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(37), BinaryOp.BITWISE_AND, new Int(1)));

        assertThat(result, equalTo(new Constant(new Int(1))));
    }

    @Test
    void derivesConstantForBitwiseAndOfConstantLong() {
        var propagation = new ConstantPropagation();

        // 37L & 1L
        var result = propagation.analyseExpression(
            new BinOp(Type.LONG, new Long(37L), BinaryOp.BITWISE_AND, new Long(1L)));

        assertThat(result, equalTo(new Constant(new Long(1L))));
    }

    @Test
    void derivesConstantForBitwiseAndOfConstantBoolean() {
        var propagation = new ConstantPropagation();

        // true & false
        var result = propagation.analyseExpression(
            new BinOp(Type.BOOLEAN, new Boolean(true), BinaryOp.BITWISE_AND, new Boolean(false)));

        assertThat(result, equalTo(new Constant(new Boolean(false))));
    }

    // Bitwise or
    @Test
    void derivesConstantForBitwiseOrOfConstantInt() {
        var propagation = new ConstantPropagation();

        // 36 | 1
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(36), BinaryOp.BITWISE_OR, new Int(1)));

        assertThat(result, equalTo(new Constant(new Int(37))));
    }

    @Test
    void derivesConstantForBitwiseOrOfConstantLong() {
        var propagation = new ConstantPropagation();

        // 36L | 1L
        var result = propagation.analyseExpression(
            new BinOp(Type.LONG, new Long(36L), BinaryOp.BITWISE_OR, new Long(1L)));

        assertThat(result, equalTo(new Constant(new Long(37L))));
    }

    @Test
    void derivesConstantForBitwiseOrOfConstantBoolean() {
        var propagation = new ConstantPropagation();

        // false | true
        var result = propagation.analyseExpression(
            new BinOp(Type.BOOLEAN, new Boolean(false), BinaryOp.BITWISE_OR, new Boolean(true)));

        assertThat(result, equalTo(new Constant(new Boolean(true))));
    }

    // Bitwise xor
    @Test
    void derivesConstantForBitwiseXorOfConstantInt() {
        var propagation = new ConstantPropagation();

        // 37 ^ 1
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(37), BinaryOp.BITWISE_XOR, new Int(1)));

        assertThat(result, equalTo(new Constant(new Int(36))));
    }

    @Test
    void derivesConstantForBitwiseXorOfConstantLong() {
        var propagation = new ConstantPropagation();

        // 37L ^ 1L
        var result = propagation.analyseExpression(
            new BinOp(Type.LONG, new Long(37L), BinaryOp.BITWISE_XOR, new Long(1L)));

        assertThat(result, equalTo(new Constant(new Long(36L))));
    }

    @Test
    void derivesConstantForBitwiseXorOfConstantBoolean() {
        var propagation = new ConstantPropagation();

        // true ^ true
        var result = propagation.analyseExpression(
            new BinOp(Type.BOOLEAN, new Boolean(true), BinaryOp.BITWISE_XOR, new Boolean(true)));

        assertThat(result, equalTo(new Constant(new Boolean(false))));
    }

    // Less than
    @Test
    void derivesBooleanConstantForLessThanOfConstantInts() {
        var propagation = new ConstantPropagation();

        // 37 < 5
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(37), BinaryOp.LESS_THAN, new Int(5)));

        assertThat(result, equalTo(new Constant(new Boolean(false))));
    }

    @Test
    void derivesBooleanConstantForLessThanOfConstantLongs() {
        var propagation = new ConstantPropagation();

        // 37L < 5L
        var result = propagation.analyseExpression(
            new BinOp(Type.LONG, new Long(37L), BinaryOp.LESS_THAN, new Long(5L)));

        assertThat(result, equalTo(new Constant(new Boolean(false))));
    }

    @Test
    void derivesBooleanConstantForLessThanOfConstantFloats() {
        var propagation = new ConstantPropagation();

        // 37.0F < 5.0F
        var result = propagation.analyseExpression(
            new BinOp(Type.FLOAT, new Float(37.0F), BinaryOp.LESS_THAN, new Float(5.0F)));

        assertThat(result, equalTo(new Constant(new Boolean(false))));
    }

    @Test
    void derivesBooleanConstantForLessThanOfConstantDoubles() {
        var propagation = new ConstantPropagation();

        // 37.0 < 5.0
        var result = propagation.analyseExpression(
            new BinOp(Type.DOUBLE, new Double(37.0), BinaryOp.LESS_THAN, new Double(5.0)));

        assertThat(result, equalTo(new Constant(new Boolean(false))));
    }

    // Less than or equal
    @Test
    void derivesBooleanConstantForLessOrEqualThanOfConstantInts() {
        var propagation = new ConstantPropagation();

        // 37 <= 37
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(37), BinaryOp.LESS_THAN_EQUAL, new Int(37)));

        assertThat(result, equalTo(new Constant(new Boolean(true))));
    }

    @Test
    void derivesBooleanConstantForLessThanOrEqualOfConstantLongs() {
        var propagation = new ConstantPropagation();

        // 37L <= 37L
        var result = propagation.analyseExpression(
            new BinOp(Type.LONG, new Long(37L), BinaryOp.LESS_THAN_EQUAL, new Long(37L)));

        assertThat(result, equalTo(new Constant(new Boolean(true))));
    }

    @Test
    void derivesBooleanConstantForLessThanOrEqualOfConstantFloats() {
        var propagation = new ConstantPropagation();

        // 37.0F <= 37.0F
        var result = propagation.analyseExpression(
            new BinOp(Type.FLOAT, new Float(37.0F), BinaryOp.LESS_THAN_EQUAL, new Float(37.0F)));

        assertThat(result, equalTo(new Constant(new Boolean(true))));
    }

    @Test
    void derivesBooleanConstantForLessThanOrEqualOfConstantDoubles() {
        var propagation = new ConstantPropagation();

        // 37.0 <= 37.0
        var result = propagation.analyseExpression(
            new BinOp(Type.DOUBLE, new Double(37.0), BinaryOp.LESS_THAN_EQUAL, new Double(37.0)));

        assertThat(result, equalTo(new Constant(new Boolean(true))));
    }

    // Greater than
    @Test
    void derivesBooleanConstantForGreaterThanOfConstantInts() {
        var propagation = new ConstantPropagation();

        // 37 > 5
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(37), BinaryOp.GREATER_THAN, new Int(5)));

        assertThat(result, equalTo(new Constant(new Boolean(true))));
    }

    @Test
    void derivesBooleanConstantForGreaterThanOfConstantLongs() {
        var propagation = new ConstantPropagation();

        // 37L < 5L
        var result = propagation.analyseExpression(
            new BinOp(Type.LONG, new Long(37L), BinaryOp.GREATER_THAN, new Long(5L)));

        assertThat(result, equalTo(new Constant(new Boolean(true))));
    }

    @Test
    void derivesBooleanConstantForGreaterThanOfConstantFloats() {
        var propagation = new ConstantPropagation();

        // 37.0F < 5.0F
        var result = propagation.analyseExpression(
            new BinOp(Type.FLOAT, new Float(37.0F), BinaryOp.GREATER_THAN, new Float(5.0F)));

        assertThat(result, equalTo(new Constant(new Boolean(true))));
    }

    @Test
    void derivesBooleanConstantForGreaterThanOfConstantDoubles() {
        var propagation = new ConstantPropagation();

        // 37.0 < 5.0
        var result = propagation.analyseExpression(
            new BinOp(Type.DOUBLE, new Double(37.0), BinaryOp.GREATER_THAN, new Double(5.0)));

        assertThat(result, equalTo(new Constant(new Boolean(true))));
    }

    // Greater than or equal
    @Test
    void derivesBooleanConstantForGreaterThanOrEqualOfConstantInts() {
        var propagation = new ConstantPropagation();

        // 37 >= 37
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(37), BinaryOp.GREATER_THAN_EQUAL, new Int(37)));

        assertThat(result, equalTo(new Constant(new Boolean(true))));
    }

    @Test
    void derivesBooleanConstantForGreaterThanOrEqualOfConstantLongs() {
        var propagation = new ConstantPropagation();

        // 37L >= 37L
        var result = propagation.analyseExpression(
            new BinOp(Type.LONG, new Long(37L), BinaryOp.GREATER_THAN_EQUAL, new Long(37L)));

        assertThat(result, equalTo(new Constant(new Boolean(true))));
    }

    @Test
    void derivesBooleanConstantForGreaterThanOrEqualOfConstantFloats() {
        var propagation = new ConstantPropagation();

        // 37.0F <= 37.0F
        var result = propagation.analyseExpression(
            new BinOp(Type.FLOAT, new Float(37.0F), BinaryOp.GREATER_THAN_EQUAL, new Float(37.0F)));

        assertThat(result, equalTo(new Constant(new Boolean(true))));
    }

    @Test
    void derivesBooleanConstantForGreaterThanOrEqualOfConstantDoubles() {
        var propagation = new ConstantPropagation();

        // 37.0 <= 37.0
        var result = propagation.analyseExpression(
            new BinOp(Type.DOUBLE, new Double(37.0), BinaryOp.GREATER_THAN_EQUAL, new Double(37.0)));

        assertThat(result, equalTo(new Constant(new Boolean(true))));
    }

    // Boolean and
    @Test
    void derivesConstantForShortCircuitingBooleanAnd() {
        var propagation = new ConstantPropagation();
        var varName = new LocalName("varName", 0);

        // false && varName
        var result = propagation.analyseExpression(
            new BinOp(Type.BOOLEAN, new Boolean(false), BinaryOp.BOOLEAN_AND, new Reference(varName, Type.BOOLEAN)));

        assertThat(result, equalTo(new Constant(new Boolean(false))));
    }

    @Test
    void derivesConstantForBooleanAndOfConstants() {
        var propagation = new ConstantPropagation();

        // true && false
        var result = propagation.analyseExpression(
            new BinOp(Type.BOOLEAN, new Boolean(true), BinaryOp.BOOLEAN_AND, new Boolean(false)));

        assertThat(result, equalTo(new Constant(new Boolean(false))));
    }

    // Boolean or
    @Test
    void derivesConstantForShortCircuitingBooleanOr() {
        var propagation = new ConstantPropagation();
        var varName = new LocalName("varName", 0);

        // true || varName
        var result = propagation.analyseExpression(
            new BinOp(Type.BOOLEAN, new Boolean(true), BinaryOp.BOOLEAN_OR, new Reference(varName, Type.BOOLEAN)));

        assertThat(result, equalTo(new Constant(new Boolean(true))));
    }

    @Test
    void derivesConstantForBooleanOrOfConstants() {
        var propagation = new ConstantPropagation();

        // false || true
        var result = propagation.analyseExpression(
            new BinOp(Type.BOOLEAN, new Boolean(false), BinaryOp.BOOLEAN_OR, new Boolean(true)));

        assertThat(result, equalTo(new Constant(new Boolean(true))));
    }

    // Equals
    @Property
    void derivesConstantForEqualsOfLiterals(@ForAll("literals") Literal left, @ForAll("literals") Literal right) {
        var propagation = new ConstantPropagation();

        // e.g. 1 == 1, 1 == 3, ...
        var result = propagation.analyseExpression(
            new BinOp(Type.BOOLEAN, left, BinaryOp.EQUAL, right));

        assertThat(result, equalTo(new Constant(new Boolean(left.equals(right)))));
    }

    @Property
    void derivesConstantForEqualsOfConstantVariables(@ForAll("literals") Literal left, @ForAll("literals") Literal right) {
        var leftName = new LocalName("left", 0);
        var rightName = new LocalName("right", 0);
        var propagation = new ConstantPropagation(Maps.mutable.of(leftName, new Constant(left), rightName, new Constant(right)));

        // left == right
        var result = propagation.analyseExpression(
            new BinOp(Type.BOOLEAN, new Reference(leftName, left.type()), BinaryOp.EQUAL, new Reference(rightName, right.type())));

        assertThat(result, equalTo(new Constant(new Boolean(left.equals(right)))));
    }

    // Not equals
    @Property
    void derivesConstantForNotEqualsOfLiterals(@ForAll("literals") Literal left, @ForAll("literals") Literal right) {
        var propagation = new ConstantPropagation();

        // e.g. 1 != 1, 1 != 3, ...
        var result = propagation.analyseExpression(
            new BinOp(Type.BOOLEAN, left, BinaryOp.NOT_EQUAL, right));

        assertThat(result, equalTo(new Constant(new Boolean(!left.equals(right)))));
    }

    @Property
    void derivesConstantForNotEqualsOfConstantVariables(@ForAll("literals") Literal left, @ForAll("literals") Literal right) {
        var leftName = new LocalName("left", 0);
        var rightName = new LocalName("right", 0);
        var propagation = new ConstantPropagation(Maps.mutable.of(leftName, new Constant(left), rightName, new Constant(right)));

        // left != right
        var result = propagation.analyseExpression(
            new BinOp(Type.BOOLEAN, new Reference(leftName, left.type()), BinaryOp.NOT_EQUAL, new Reference(rightName, right.type())));

        assertThat(result, equalTo(new Constant(new Boolean(!left.equals(right)))));
    }

    // Patterns
    @Test
    void derivesUnknownForIdentifierPattern() {
        var propagation = new ConstantPropagation();
        var idName = new LocalName("bool", 0);

        // bool
        propagation.analysePattern(new IdPattern(idName, Type.BOOLEAN));

        assertThat(propagation.getEnvironment().get(idName), equalTo(Unknown.VALUE));
    }

    @Test
    void derivesUnderlyingForAliasPattern() {
        var propagation = new ConstantPropagation();
        var literal = new Boolean(true);
        var alias = new LocalName("bool", 0);

        // bool @ true
        propagation.analysePattern(new AliasPattern(alias, Type.BOOLEAN, new LiteralPattern(literal)));

        assertThat(propagation.getEnvironment().get(alias), equalTo(new Constant(literal)));
    }

    @Test
    void derivesKnownConstructorForConstructorPattern() {
        var propagation = new ConstantPropagation();
        var namespaceName = new NamespaceName(Lists.immutable.of("Mina", "Test"), "Constants");
        var dataName = new DataName(new QualifiedName(namespaceName, "List"));
        var constrName = new ConstructorName(dataName, new QualifiedName(namespaceName, "Nil"));

        // Nil {}
        var result = propagation.analysePattern(
            new ConstructorPattern(
                constrName,
                new TypeConstructor(constrName.name(), TypeKind.INSTANCE),
                Lists.immutable.empty()));

        assertThat(result, equalTo(new KnownConstructor(constrName)));
    }

    @Property
    void derivesConstantForLiteralPattern(@ForAll("literals") Literal literal) {
        var propagation = new ConstantPropagation();

        var result = propagation.analysePattern(new LiteralPattern(literal));

        assertThat(result, equalTo(new Constant(literal)));
    }

    // Literals and references
    @Property
    void derivesConstantForLiteral(@ForAll("literals") Literal literal) {
        var propagation = new ConstantPropagation();
        var result = propagation.analyseExpression(literal);
        assertThat(result, equalTo(new Constant(literal)));
    }

    @Property
    void derivesConstantForReferenceWithConstantValue(@ForAll("literals") Literal literal) {
        var varName = new LocalName("varName", 0);
        var knownValue = new Constant(literal);
        var propagation = new ConstantPropagation(Maps.mutable.of(varName, knownValue));
        var result = propagation.analyseExpression(new Reference(varName, literal.type()));
        assertThat(result, equalTo(knownValue));
    }

    @Property
    void derivesUnknownForReferenceWithUnknownValue(@ForAll("literals") Literal literal) {
        var varName = new LocalName("varName", 0);
        var propagation = new ConstantPropagation();
        var result = propagation.analyseExpression(new Reference(varName, literal.type()));
        assertThat(result, equalTo(Unknown.VALUE));
    }

    @Test
    void derivesNonConstantForReferenceToFunction() {
        var varName = new LocalName("const", 0);
        var propagation = new ConstantPropagation();
        var result = propagation.analyseExpression(new Reference(varName, Type.function(Type.INT, Type.INT)));
        assertThat(result, equalTo(NonConstant.VALUE));
    }

    @Test
    void derivesNonConstantForReferenceToPolymorphicFunction() {
        var varName = new LocalName("const", 0);
        var propagation = new ConstantPropagation();
        var tyVarA = new ForAllVar("A", TypeKind.INSTANCE);
        var tyVarB = new ForAllVar("B", TypeKind.INSTANCE);
        var funTy = Type.function(tyVarA, tyVarB, tyVarA);
        var polyFunTy = new QuantifiedType(Lists.immutable.of(tyVarA, tyVarB), funTy, TypeKind.INSTANCE);
        var result = propagation.analyseExpression(new Reference(varName, polyFunTy));
        assertThat(result, equalTo(NonConstant.VALUE));
    }

    @Property
    void derivesUnderlyingForBox(@ForAll("literals") Literal literal) {
        var propagation = new ConstantPropagation();
        var result = propagation.analyseExpression(new Box(literal));
        assertThat(result, equalTo(new Constant(literal)));
    }

    @Property
    void derivesUnderlyingForUnbox(@ForAll("literals") Literal literal) {
        var propagation = new ConstantPropagation();
        var result = propagation.analyseExpression(new Unbox(literal));
        assertThat(result, equalTo(new Constant(literal)));
    }

    @Provide
    Arbitrary<Literal> literals() {
        return Arbitraries.oneOf(
            Arbitraries.chars().map(Char::new),
            Arbitraries.doubles().map(Double::new),
            Arbitraries.floats().map(Float::new),
            Arbitraries.integers().map(Int::new),
            Arbitraries.longs().map(Long::new),
            Arbitraries.strings().map(String::new)
        );
    }
}

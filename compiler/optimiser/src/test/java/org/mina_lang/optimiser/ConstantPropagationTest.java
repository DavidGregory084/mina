/*
 * SPDX-FileCopyrightText:  © 2026 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.optimiser;

import net.jqwik.api.*;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.impl.factory.Lists;
import org.junit.jupiter.api.Test;
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
import org.mina_lang.optimiser.constants.Constant;
import org.mina_lang.optimiser.constants.KnownConstructor;
import org.mina_lang.optimiser.constants.NonConstant;
import org.mina_lang.optimiser.constants.Unassigned;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ConstantPropagationTest {
    // Conditional expressions
    @Test
    void derivesUnassignedForIfWhenCondUnassigned() {
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

        assertThat(result, equalTo(Unassigned.VALUE));
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
                Type.INT,
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
                Type.INT,
                new Reference(varName, Type.BOOLEAN),
                new String("true"),
                new String("true")));

        assertThat(result, equalTo(new Constant(new String("true"))));
    }

    @Test
    void derivesUnassignedForMatchWhenScrutineeUnassigned() {
        var varName = new LocalName("bool", 0);
        var propagation = new ConstantPropagation();

        // match bool with { case true -> false; case false -> true }
        // no known assignment for bool
        var result = propagation.analyseExpression(
            new Match(
                Type.INT,
                new Reference(varName, Type.BOOLEAN),
                Lists.immutable.of(
                    new Case(new LiteralPattern(new Boolean(true)), new Boolean(false)),
                    new Case(new LiteralPattern(new Boolean(false)), new Boolean(true)))));

        assertThat(result, equalTo(Unassigned.VALUE));
    }

    @Test
    void derivesConstantForConstantMatchCases() {
        var varName = new LocalName("bool", 0);
        var propagation = new ConstantPropagation(Maps.mutable.of(varName, NonConstant.VALUE));

        // match bool with { case true -> true; case false -> true }
        // bool known to be non-constant
        var result = propagation.analyseExpression(
            new Match(
                Type.INT,
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
                Type.INT,
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
    void derivesUnassignedForIntDivisionByZero() {
        var propagation = new ConstantPropagation();

        // 8 / 0
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(8), BinaryOp.DIVIDE, new Int(0)));

        assertThat(result, equalTo(Unassigned.VALUE));
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
    void derivesUnassignedForIntegerModulusByZero() {
        var propagation = new ConstantPropagation();

        // 9 % 0
        var result = propagation.analyseExpression(
            new BinOp(Type.INT, new Int(9), BinaryOp.MODULUS, new Int(0)));

        assertThat(result, equalTo(Unassigned.VALUE));
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

    // Patterns
    @Test
    void derivesUnassignedForIdentifierPattern() {
        var propagation = new ConstantPropagation();
        var idName = new LocalName("bool", 0);

        // bool
        propagation.analysePattern(new IdPattern(idName, Type.BOOLEAN));

        assertThat(propagation.getEnvironment().get(idName), equalTo(Unassigned.VALUE));
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
    void derivesUnassignedForReferenceWithUnknownValue(@ForAll("literals") Literal literal) {
        var varName = new LocalName("varName", 0);
        var propagation = new ConstantPropagation();
        var result = propagation.analyseExpression(new Reference(varName, literal.type()));
        assertThat(result, equalTo(Unassigned.VALUE));
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

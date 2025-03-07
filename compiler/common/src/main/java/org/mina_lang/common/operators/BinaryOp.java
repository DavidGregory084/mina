/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.operators;

import org.eclipse.collections.api.factory.Sets;

import java.util.Set;

public enum BinaryOp {
    MULTIPLY,
    DIVIDE,
    MODULUS,
    ADD,
    SUBTRACT,
    SHIFT_LEFT,
    SHIFT_RIGHT,
    UNSIGNED_SHIFT_RIGHT,
    BITWISE_AND,
    BITWISE_OR,
    BITWISE_XOR,
    LESS_THAN,
    LESS_THAN_EQUAL,
    GREATER_THAN,
    GREATER_THAN_EQUAL,
    EQUAL,
    NOT_EQUAL,
    BOOLEAN_AND,
    BOOLEAN_OR;

    public static final Set<BinaryOp> VALUES =
        Set.of(values());

    public static final Set<BinaryOp> ARITHMETIC_OPERATORS =
        Set.of(ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULUS);

    public static final Set<BinaryOp> BITWISE_OPERATORS =
        Set.of(BITWISE_AND, BITWISE_OR, BITWISE_XOR);

    public static final Set<BinaryOp> EQUALITY_OPERATORS =
        Set.of(EQUAL, NOT_EQUAL);

    public static final Set<BinaryOp> LOGICAL_OPERATORS =
        Set.of(BOOLEAN_AND, BOOLEAN_OR);

    public static final Set<BinaryOp> RELATIONAL_OPERATORS =
        Set.of(LESS_THAN, LESS_THAN_EQUAL, GREATER_THAN, GREATER_THAN_EQUAL);

    public static final Set<BinaryOp> BOOLEAN_OPERATORS =
        Sets.mutable
            .ofAll(EQUALITY_OPERATORS)
            .withAll(LOGICAL_OPERATORS)
            .withAll(RELATIONAL_OPERATORS);

    public static final Set<BinaryOp> SHIFT_OPERATORS =
        Set.of(SHIFT_LEFT, SHIFT_RIGHT, UNSIGNED_SHIFT_RIGHT);
}

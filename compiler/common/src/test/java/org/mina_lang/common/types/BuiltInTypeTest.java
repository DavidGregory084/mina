/*
 * SPDX-FileCopyrightText:  © 2025 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

import com.jparams.verifier.tostring.ToStringVerifier;
import net.jqwik.api.*;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BuiltInTypeTest {
    @Test
    void testEquals() {
        EqualsVerifier
            .forClass(BuiltInType.class)
            .withPrefabValues(BuiltInType.class, Type.INT, Type.BOOLEAN)
            .withPrefabValuesForField("display", new BuiltInType[5], new BuiltInType[]{Type.INT, null, null, null, null})
            .withPrefabValuesForField("kind", TypeKind.INSTANCE, new HigherKind(TypeKind.INSTANCE, TypeKind.INSTANCE))
            .verify();
    }

    @Test
    void testToString() {
        ToStringVerifier
            .forClass(BuiltInType.class)
            .verify();
    }

    @Test
    void intSubtypesLong() {
        assertTrue(Type.INT.isSubtypeOf(Type.LONG));
    }

    @Test
    void longNotSubtypesInt() {
        assertFalse(Type.LONG.isSubtypeOf(Type.INT));
    }

    @Test
    void floatSubtypesDouble() {
        assertTrue(Type.FLOAT.isSubtypeOf(Type.DOUBLE));
    }

    @Test
    void doubleNotSubtypesFloat() {
        assertFalse(Type.DOUBLE.isSubtypeOf(Type.FLOAT));
    }

    @Property
    void subtypingReflexive(@ForAll("builtInTypes") BuiltInType builtIn) {
        assertTrue(builtIn.isSubtypeOf(builtIn));
    }

    @Property(tries = 1000)
    void subtypingTransitive(@ForAll("builtInTypes") BuiltInType a, @ForAll("builtInTypes") BuiltInType b, @ForAll("builtInTypes") BuiltInType c) {
        if (a.isSubtypeOf(b) && b.isSubtypeOf(c)) {
            assertTrue(a.isSubtypeOf(c));
        }
    }

    @Provide
    Arbitrary<BuiltInType> builtInTypes() {
        return Arbitraries.of(Type.builtIns.toSet());
    }
}

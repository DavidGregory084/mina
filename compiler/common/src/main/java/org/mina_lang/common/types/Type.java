/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

import java.util.*;

sealed public interface Type extends Sort permits PolyType, MonoType {
    public Kind kind();

    default public boolean isPrimitive() {
        return false;
    }

    @Override
    default <A> A accept(SortFolder<A> visitor) {
        return visitor.visitType(this);
    }

    @Override
    default void accept(SortVisitor visitor) {
        visitor.visitType(this);
    }

    public void accept(TypeVisitor visitor);

    public <A> A accept(TypeFolder<A> visitor);

    public Type accept(TypeTransformer visitor);

    public static boolean isFunction(Type type) {
        if (type instanceof TypeApply tyApp) {
            if (tyApp.type() instanceof BuiltInType builtIn) {
                return "->".equals(builtIn.name());
            }
        }

        return false;
    }

    public static TypeApply function(Type... types) {
        return function(
            List.of(Arrays.copyOfRange(types, 0, types.length - 1)),
            types[types.length - 1]);
    }

    public static TypeApply function(List<Type> argTypes, Type returnType) {
        var appliedTypes = new ArrayList<>(argTypes);
        appliedTypes.add(returnType);
        var appliedKinds = Collections.<Kind>nCopies(argTypes.size() + 1, TypeKind.INSTANCE);
        var kind = new HigherKind(appliedKinds, TypeKind.INSTANCE);
        return new TypeApply(new BuiltInType("->", kind), appliedTypes, TypeKind.INSTANCE);
    }

    public static BuiltInType UNIT = new BuiltInType("Unit", TypeKind.INSTANCE);

    public static BuiltInType BOOLEAN = new BuiltInType("Boolean", TypeKind.INSTANCE);

    public static BuiltInType CHAR = new BuiltInType("Char", TypeKind.INSTANCE);

    public static BuiltInType STRING = new BuiltInType("String", TypeKind.INSTANCE);

    public static BuiltInType INT = new BuiltInType("Int", TypeKind.INSTANCE);

    public static BuiltInType LONG = new BuiltInType("Long", TypeKind.INSTANCE);

    public static BuiltInType FLOAT = new BuiltInType("Float", TypeKind.INSTANCE);

    public static BuiltInType DOUBLE = new BuiltInType("Double", TypeKind.INSTANCE);

    public static BuiltInType NAMESPACE = new BuiltInType("Namespace", TypeKind.INSTANCE);

    public static Set<BuiltInType> primitives = Set.of(Type.BOOLEAN, Type.CHAR, Type.INT, Type.LONG, Type.FLOAT, Type.DOUBLE);

    public static Map<String, BuiltInType> builtIns = Map.of(
        "Unit", Type.UNIT,
        "Boolean", Type.BOOLEAN,
        "Char", Type.CHAR,
        "String", Type.STRING,
        "Int", Type.INT,
        "Long", Type.LONG,
        "Float", Type.FLOAT,
        "Double", Type.DOUBLE);
}

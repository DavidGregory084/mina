/*
 * SPDX-FileCopyrightText:  © 2022-2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.common.types;

import java.util.Arrays;
import java.util.Objects;

public record BuiltInType(String name, Kind kind, int depth, BuiltInType[] display) implements MonoType {
    private static final int DISPLAY_LENGTH = 5;

    public BuiltInType(String name, Kind kind) {
        this(name, kind, 0, new BuiltInType[DISPLAY_LENGTH]);
    }

    public BuiltInType(String name, Kind kind, BuiltInType superType) {
        this(name, kind, superType.depth + 1, Arrays.copyOf(superType.display, DISPLAY_LENGTH));
        this.display[superType.depth] = superType;
    }

    public boolean isSubtypeOf(BuiltInType other) {
        return this.equals(other) || other.equals(this.display[other.depth]);
    }

    @Override
    public boolean isPrimitive() {
        return Type.primitives.contains(this);
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visitBuiltInType(this);
    }

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitBuiltInType(this);
    }

    @Override
    public BuiltInType accept(TypeTransformer visitor) {
        return visitor.visitBuiltInType(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BuiltInType that)) return false;
        return depth == that.depth
            && Objects.equals(kind, that.kind)
            && Objects.equals(name, that.name)
            && Objects.deepEquals(display, that.display);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, kind, depth, Arrays.hashCode(display));
    }

    @Override
    public String toString() {
        return "BuiltInType[" +
            "name='" + name + '\'' +
            ", kind=" + kind +
            ", depth=" + depth +
            ", display=" + Arrays.toString(display) +
            ']';
    }
}

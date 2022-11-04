package org.mina_lang.common.types;

import org.eclipse.collections.api.factory.Lists;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.common.names.QualifiedName;

sealed public interface Type extends Sort permits PolyType, MonoType {
    public Polarity polarity();

    public Kind kind();

    public <A> A accept(TypeFolder<A> visitor);

    public Type accept(TypeTransformer visitor);

    public static Type BOOLEAN = new TypeConstructor(
            new QualifiedName(new NamespaceName(Lists.immutable.empty(), "Primitives"), "Boolean"), Star.INSTANCE);

    public static Type CHAR = new TypeConstructor(
            new QualifiedName(new NamespaceName(Lists.immutable.empty(), "Primitives"), "Char"), Star.INSTANCE);

    public static Type STRING = new TypeConstructor(
            new QualifiedName(new NamespaceName(Lists.immutable.empty(), "Primitives"), "String"), Star.INSTANCE);

    public static Type INT = new TypeConstructor(
            new QualifiedName(new NamespaceName(Lists.immutable.empty(), "Primitives"), "Int"), Star.INSTANCE);

    public static Type LONG = new TypeConstructor(
            new QualifiedName(new NamespaceName(Lists.immutable.empty(), "Primitives"), "Long"), Star.INSTANCE);

    public static Type FLOAT = new TypeConstructor(
            new QualifiedName(new NamespaceName(Lists.immutable.empty(), "Primitives"), "Float"), Star.INSTANCE);

    public static Type DOUBLE = new TypeConstructor(
            new QualifiedName(new NamespaceName(Lists.immutable.empty(), "Primitives"), "Double"), Star.INSTANCE);
}

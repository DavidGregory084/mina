package org.mina_lang.common.types;

import org.eclipse.collections.api.factory.Lists;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.common.names.QualifiedName;

sealed public interface Type extends Sort permits PolyType, MonoType {
    public Polarity polarity();

    public Kind kind();

    public <A> A accept(TypeFolder<A> visitor);

    public Type accept(TypeTransformer visitor);

    public static Type BOOLEAN = new BuiltInType("Boolean", TypeKind.INSTANCE);

    public static Type CHAR = new BuiltInType("Char", TypeKind.INSTANCE);

    public static Type STRING = new BuiltInType("String", TypeKind.INSTANCE);

    public static Type INT = new BuiltInType("Int", TypeKind.INSTANCE);

    public static Type LONG = new BuiltInType("Long", TypeKind.INSTANCE);

    public static Type FLOAT = new BuiltInType("Float", TypeKind.INSTANCE);

    public static Type DOUBLE = new BuiltInType("Double", TypeKind.INSTANCE);
}

package org.mina_lang.common.types;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.ImmutableMap;

sealed public interface Type extends Sort permits PolyType, MonoType {
    public Polarity polarity();

    public Kind kind();

    @Override
    default <A> A accept(SortFolder<A> visitor) {
        return visitor.visitType(this);
    }

    public <A> A accept(TypeFolder<A> visitor);

    public Type accept(TypeTransformer visitor);

    default public Type substitute(
            UnionFind<MonoType> typeSubstitution,
            UnionFind<Kind> kindSubstitution) {
        return accept(new TypeSubstitutionTransformer(typeSubstitution, kindSubstitution));
    }

    default public Type defaultKinds() {
        return accept(new TypeSubstitutionTransformer(new KindDefaultingTransformer()));
    }

    public static boolean isFunction(Type type) {
        if (type instanceof TypeApply tyApp) {
            if (tyApp.type() instanceof BuiltInType builtIn) {
                return "->".equals(builtIn.name());
            }
        }

        return false;
    }

    public static TypeApply function(ImmutableList<Type> argTypes, Type returnType) {
        var appliedTypes = argTypes.newWith(returnType);
        var kind = new HigherKind(appliedTypes.collect(typ -> TypeKind.INSTANCE), TypeKind.INSTANCE);
        return new TypeApply(new BuiltInType("->", kind), appliedTypes, TypeKind.INSTANCE);
    }

    public static BuiltInType BOOLEAN = new BuiltInType("Boolean", TypeKind.INSTANCE);

    public static BuiltInType CHAR = new BuiltInType("Char", TypeKind.INSTANCE);

    public static BuiltInType STRING = new BuiltInType("String", TypeKind.INSTANCE);

    public static BuiltInType INT = new BuiltInType("Int", TypeKind.INSTANCE);

    public static BuiltInType LONG = new BuiltInType("Long", TypeKind.INSTANCE);

    public static BuiltInType FLOAT = new BuiltInType("Float", TypeKind.INSTANCE);

    public static BuiltInType DOUBLE = new BuiltInType("Double", TypeKind.INSTANCE);

    public static BuiltInType NAMESPACE = new BuiltInType("Namespace", TypeKind.INSTANCE);

    public static ImmutableMap<String, BuiltInType> builtIns = Maps.immutable.<String, BuiltInType>empty()
            .newWithKeyValue("Boolean", Type.BOOLEAN)
            .newWithKeyValue("Char", Type.CHAR)
            .newWithKeyValue("String", Type.STRING)
            .newWithKeyValue("Int", Type.INT)
            .newWithKeyValue("Long", Type.LONG)
            .newWithKeyValue("Float", Type.FLOAT)
            .newWithKeyValue("Double", Type.DOUBLE);
}

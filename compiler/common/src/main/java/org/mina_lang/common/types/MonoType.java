package org.mina_lang.common.types;

public sealed interface MonoType extends Type permits TypeConstructor, BuiltInType, TypeApply, TypeVar, UnsolvedType {

    @Override
    MonoType accept(TypeTransformer visitor);
}

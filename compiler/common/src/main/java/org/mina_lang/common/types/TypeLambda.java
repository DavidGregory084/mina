package org.mina_lang.common.types;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.TypeEnvironment;
import org.mina_lang.common.names.ExistsVarName;
import org.mina_lang.common.names.ForAllVarName;

public record TypeLambda(ImmutableList<TypeVar> args, Type body, Kind kind) implements PolyType {

    @Override
    public <A> A accept(TypeFolder<A> visitor) {
        return visitor.visitTypeLambda(this);
    }

    @Override
    public TypeLambda accept(TypeTransformer visitor) {
        return visitor.visitTypeLambda(this);
    }

    public TypeInstantiationTransformer subTypeInstantiationIn(TypeEnvironment environment, UnsolvedVariableSupply varSupply) {
        var instantiated = Maps.mutable.<TypeVar, MonoType>empty();

        args().forEach(tyParam -> {
            if (tyParam instanceof ForAllVar forall) {
                var unsolved = varSupply.newUnsolvedType(forall.kind());
                environment.putUnsolvedType(unsolved);
                instantiated.put(forall, unsolved);
            } else if (tyParam instanceof ExistsVar exists) {
                var typeVarName = new ExistsVarName(exists.name());
                var typeVarAttrs = new Attributes(typeVarName, exists.kind());
                environment.putType(exists.name(), Meta.of(typeVarAttrs));
            }
        });

        return new TypeInstantiationTransformer(instantiated.toImmutable());
    }

    public Type instantiateAsSubTypeIn(TypeEnvironment environment, UnsolvedVariableSupply varSupply) {
        return body().accept(subTypeInstantiationIn(environment, varSupply));
    }

    public TypeInstantiationTransformer superTypeInstantiationIn(TypeEnvironment environment, UnsolvedVariableSupply varSupply) {
        var instantiated = Maps.mutable.<TypeVar, MonoType>empty();

        args().forEach(tyParam -> {
            if (tyParam instanceof ForAllVar forall) {
                var typeVarName = new ForAllVarName(forall.name());
                var typeVarAttrs = new Attributes(typeVarName, forall.kind());
                environment.putType(forall.name(), Meta.of(typeVarAttrs));
            } else if (tyParam instanceof ExistsVar exists) {
                var unsolved = varSupply.newUnsolvedType(exists.kind());
                environment.putUnsolvedType(unsolved);
                instantiated.put(exists, unsolved);
            }
        });

        return new TypeInstantiationTransformer(instantiated.toImmutable());
    }

    public Type instantiateAsSuperTypeIn(TypeEnvironment environment, UnsolvedVariableSupply varSupply) {
        return body().accept(superTypeInstantiationIn(environment, varSupply));
    }
}

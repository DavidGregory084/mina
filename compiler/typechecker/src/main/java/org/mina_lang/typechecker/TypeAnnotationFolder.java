package org.mina_lang.typechecker;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Environment;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.BuiltInName;
import org.mina_lang.common.names.TypeName;
import org.mina_lang.common.names.TypeVarName;
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.QualifiedIdNode;
import org.mina_lang.syntax.TypeNodeFolder;

public class TypeAnnotationFolder implements TypeNodeFolder<Attributes, Type> {

    protected Environment<Attributes> environment;
    protected MutableMap<String, TypeVar> tyVars = Maps.mutable.empty();

    public TypeAnnotationFolder(Environment<Attributes> environment) {
        this.environment = environment;
    }

    @Override
    public TypeApply visitFunType(Meta<Attributes> meta, ImmutableList<Type> argTypes, Type returnType) {
        return Type.function(argTypes, returnType);
    }

    @Override
    public TypeApply visitTypeApply(Meta<Attributes> meta, Type type, ImmutableList<Type> args) {
        return new TypeApply(type, args, (Kind) meta.meta().sort());
    }

    @Override
    public Type visitTypeReference(Meta<Attributes> meta, QualifiedIdNode id) {
        var name = meta.meta().name();

        if (name instanceof BuiltInName builtInName) {
            // Built in types
            return Type.builtIns.get(builtInName.name());
        } else if (name instanceof TypeVarName) {
            // Bound type variables
            return tyVars.get(id.canonicalName());
        } else if (name instanceof TypeName typeName) {
            // Data types
            var typeMeta = environment.lookupType(id.canonicalName()).get();
            return new TypeConstructor(
                    typeName.name(),
                    (Kind) typeMeta.meta().sort());
        }

        return null;
    }

    @Override
    public TypeLambda visitTypeLambda(Meta<Attributes> meta, ImmutableList<Type> args, Type body) {
        return new TypeLambda(
                args.collect(tyArg -> (TypeVar) tyArg),
                body,
                (Kind) meta.meta().sort());
    }

    @Override
    public ForAllVar visitForAllVar(Meta<Attributes> meta, String name) {
        var forAllVar = new ForAllVar(name, (Kind) meta.meta().sort());
        tyVars.put(name, forAllVar);
        return forAllVar;
    }

    @Override
    public ExistsVar visitExistsVar(Meta<Attributes> meta, String name) {
        var existsVar = new ExistsVar(name, (Kind) meta.meta().sort());
        tyVars.put(name, existsVar);
        return existsVar;
    }
}
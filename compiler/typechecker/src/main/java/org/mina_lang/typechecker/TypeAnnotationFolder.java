package org.mina_lang.typechecker;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Environment;
import org.mina_lang.common.Meta;
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.QualifiedIdNode;
import org.mina_lang.syntax.TypeNodeFolder;

public class TypeAnnotationFolder implements TypeNodeFolder<Attributes, Type> {

    private Environment<Attributes> environment;

    public TypeAnnotationFolder(Environment<Attributes> environment) {
        this.environment = environment;
    }

    @Override
    public TypeLambda visitTypeLambda(Meta<Attributes> meta, ImmutableList<Type> args, Type body) {
        return new TypeLambda(
            args.collect(tyArg -> (TypeVar) tyArg),
            body,
            (Kind) meta.meta().sort());
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
        var currentNamespaceScope = environment.enclosingNamespace().get();

        return new TypeConstructor(
            id.getName(currentNamespaceScope.namespace()),
            (Kind) meta.meta().sort());
    }

    @Override
    public ForAllVar visitForAllVar(Meta<Attributes> meta, String name) {
        return new ForAllVar(name, (Kind) meta.meta().sort());
    }

    @Override
    public ExistsVar visitExistsVar(Meta<Attributes> meta, String name) {
        return new ExistsVar(name, (Kind) meta.meta().sort());
    }
}

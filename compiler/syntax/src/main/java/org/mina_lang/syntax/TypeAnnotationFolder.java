package org.mina_lang.syntax;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.TypeEnvironment;
import org.mina_lang.common.names.BuiltInName;
import org.mina_lang.common.names.ExistsVarName;
import org.mina_lang.common.names.ForAllVarName;
import org.mina_lang.common.names.TypeName;
import org.mina_lang.common.scopes.typing.TypeLambdaTypingScope;
import org.mina_lang.common.types.*;

public class TypeAnnotationFolder implements TypeNodeFolder<Attributes, Type> {

    protected TypeEnvironment environment;

    public TypeAnnotationFolder(TypeEnvironment environment) {
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
            return Type.builtIns.get(builtInName.name());
        } else if (name instanceof ExistsVarName exists) {
            var existsMeta = environment.lookupType(exists.name()).get();
            var existsKind = (Kind) existsMeta.meta().sort();
            return new ExistsVar(exists.name(), existsKind);
        } else if (name instanceof ForAllVarName forall) {
            var forallMeta = environment.lookupType(forall.name()).get();
            var forallKind = (Kind) forallMeta.meta().sort();
            return new ForAllVar(forall.name(), forallKind);
        } else if (name instanceof TypeName typeName) {
            var typeMeta = environment.lookupType(id.canonicalName()).get();
            var typeKind = (Kind) typeMeta.meta().sort();
            return new TypeConstructor(typeName.name(), typeKind);
        }

        return null;
    }

    @Override
    public void preVisitTypeLambda(TypeLambdaNode<Attributes> tyLam) {
        environment.pushScope(new TypeLambdaTypingScope());
    }

    @Override
    public TypeLambda visitTypeLambda(Meta<Attributes> meta, ImmutableList<Type> args, Type body) {
        return new TypeLambda(
                args.collect(tyArg -> (TypeVar) tyArg),
                body,
                (Kind) meta.meta().sort());
    }

    @Override
    public void postVisitTypeLambda(Type tyLam) {
        environment.popScope(TypeLambdaTypingScope.class);
    }

    @Override
    public ForAllVar visitForAllVar(Meta<Attributes> meta, String name) {
        environment.putType(name, meta);
        return new ForAllVar(name, (Kind) meta.meta().sort());
    }

    @Override
    public ExistsVar visitExistsVar(Meta<Attributes> meta, String name) {
        environment.putType(name, meta);
        return new ExistsVar(name, (Kind) meta.meta().sort());
    }
}

/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.typechecker;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.BuiltInName;
import org.mina_lang.common.names.ExistsVarName;
import org.mina_lang.common.names.ForAllVarName;
import org.mina_lang.common.names.TypeName;
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.QualifiedIdNode;
import org.mina_lang.syntax.QuantifiedTypeNode;
import org.mina_lang.syntax.TypeNodeFolder;
import org.mina_lang.typechecker.scopes.QuantifiedTypingScope;

import java.util.List;

public class TypeAnnotationFolder implements TypeNodeFolder<Attributes, Type> {

    protected TypeEnvironment environment;

    public TypeAnnotationFolder(TypeEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public TypeApply visitFunType(Meta<Attributes> meta, List<Type> argTypes, Type returnType) {
        return Type.function(argTypes, returnType);
    }

    @Override
    public TypeApply visitTypeApply(Meta<Attributes> meta, Type type, List<Type> args) {
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
    public void preVisitQuantifiedType(QuantifiedTypeNode<Attributes> quant) {
        environment.pushScope(new QuantifiedTypingScope());
    }

    @Override
    public QuantifiedType visitQuantifiedType(Meta<Attributes> meta, List<Type> args, Type body) {
        return new QuantifiedType(
            args.stream().map(tyArg -> (TypeVar) tyArg).toList(),
            body,
            (Kind) meta.meta().sort());
    }

    @Override
    public void postVisitQuantifiedType(Type quant) {
        environment.popScope(QuantifiedTypingScope.class);
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

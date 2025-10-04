/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.proto;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Scope;
import org.mina_lang.common.TopLevelScope;
import org.mina_lang.common.names.*;
import org.mina_lang.common.types.*;

import java.util.Map;

public class ProtobufReader {
    public Scope<Meta<Attributes>> fromProto(Environment env) {
        var strings = new String[env.getStringsCount()];
        var names = new Name[env.getNamesCount()];
        var sorts = new Sort[env.getSortsCount()];

        for (var i = 0; i < env.getStringsCount(); i++) {
            strings[i] = env.getStrings(i);
        }

        for (var i = 0; i < env.getNamesCount(); i++) {
            names[i] = fromProto(strings, names, env.getNames(i));
        }

        for (var i = 0; i < env.getSortsCount(); i++) {
            sorts[i] = fromProto(strings, names, sorts, env.getSorts(i));
        }

        MutableMap<String, Meta<Attributes>> values = Maps.mutable.empty();
        MutableMap<String, Meta<Attributes>> types = Maps.mutable.empty();
        loadFromProto(strings, names, sorts, env.getValuesMap(), values);
        loadFromProto(strings, names, sorts, env.getTypesMap(), types);

        MutableMap<ConstructorName, MutableMap<String, Meta<Attributes>>> fields = Maps.mutable.empty();
        for (var i = 0; i < env.getFieldsCount(); i++) {
            var entry = env.getFields(i);
            MutableMap<String, Meta<Attributes>> entryFields = Maps.mutable.empty();
            loadFromProto(strings, names, sorts, entry.getFieldsMap(), entryFields);
            fields.put((ConstructorName) names[entry.getConstructor()], entryFields);
        }

        return new TopLevelScope<>(values, types, fields);
    }

    private void loadFromProto(
        String[] strings, Name[] names, Sort[] sorts,
        Map<Integer, org.mina_lang.proto.Attributes> source,
        Map<String, Meta<Attributes>> destination
    ) {
        for (var entry : source.entrySet()) {
            destination.put(strings[entry.getKey()], fromProto(names, sorts, entry.getValue()));
        }
    }

    private Meta<Attributes> fromProto(Name[] names, Sort[] sorts, org.mina_lang.proto.Attributes proto) {
        return Meta.of(names[proto.getName()], sorts[proto.getSort()]);
    }

    private Name fromProto(String[] strings, Name[] names, org.mina_lang.proto.names.Name proto) {
        return switch (proto.getNameCase()) {
            case NAME_NOT_SET -> null;
            case FORALL -> new ForAllVarName(strings[proto.getForall().getName()]);
            case EXISTS -> new ExistsVarName(strings[proto.getExists().getName()]);
            case BUILTIN -> new BuiltInName(strings[proto.getBuiltIn().getName()]);
            case NAMESPACE -> {
                var namespace = proto.getNamespace();
                var pkgList = namespace.getPkgList().stream().map(pkg -> strings[pkg]);
                yield new NamespaceName(Lists.immutable.fromStream(pkgList), strings[namespace.getName()]);
            }
            case LET -> {
                var name = proto.getLet().getName();
                yield new LetName(new QualifiedName((NamespaceName) names[name.getNamespace()], strings[name.getName()]));
            }
            case DATA -> {
                var name = proto.getData().getName();
                yield new DataName(new QualifiedName((NamespaceName) names[name.getNamespace()], strings[name.getName()]));
            }
            case CONSTR -> {
                var constr = proto.getConstr();
                var name = constr.getName();
                yield new ConstructorName(
                    (DataName) names[constr.getEnclosing()],
                    new QualifiedName((NamespaceName) names[name.getNamespace()], strings[name.getName()])
                );
            }
            case FIELD -> {
                var field = proto.getField();
                yield new FieldName((ConstructorName) names[field.getConstr()], strings[field.getName()]);
            }
        };
    }

    private Sort fromProto(String[] strings, Name[] names, Sort[] sorts, org.mina_lang.proto.types.Sort proto) {
        return switch (proto.getSortCase()) {
            case SORT_NOT_SET -> null;
            case TYPEKIND -> TypeKind.INSTANCE;
            case HIGHERKIND -> {
                var higher = proto.getHigherKind();
                MutableList<Kind> args = Lists.mutable.of();
                for (var i = 0; i < higher.getArgsCount(); i++) {
                    args.add((Kind) sorts[higher.getArgs(i)]);
                }
                yield new HigherKind(
                    args.toImmutableList(),
                    (Kind) sorts[higher.getResult()]
                );
            }
            case BUILTIN -> {
                var builtIn = proto.getBuiltIn();
                var builtInName = strings[builtIn.getName()];
                yield Type.builtIns.getIfAbsent(
                    builtInName,
                    () -> new BuiltInType(builtInName, (Kind) sorts[builtIn.getKind()])
                );
            }
            case QUANTTY -> {
                var quant = proto.getQuantTy();
                MutableList<TypeVar> args = Lists.mutable.of();
                for (var i = 0; i < quant.getArgsCount(); i++) {
                    args.add((TypeVar) sorts[quant.getArgs(i)]);
                }
                yield new QuantifiedType(
                    args.toImmutableList(),
                    (Type) sorts[quant.getBody()],
                    (Kind) sorts[quant.getKind()]
                );
            }
            case TYCON -> {
                var tyCon = proto.getTyCon();
                var name = tyCon.getName();
                yield new TypeConstructor(
                    new QualifiedName((NamespaceName) names[name.getNamespace()], strings[name.getName()]),
                    (Kind) sorts[tyCon.getKind()]
                );
            }
            case TYAPP -> {
                var tyApp = proto.getTyApp();
                MutableList<Type> args = Lists.mutable.of();
                for (var i = 0; i < tyApp.getArgsCount(); i++) {
                    args.add((Type) sorts[tyApp.getArgs(i)]);
                }
                yield new TypeApply(
                    (Type) sorts[tyApp.getTyp()],
                    args.toImmutableList(),
                    (Kind) sorts[tyApp.getKind()]
                );
            }
            case FORALL -> {
                var forall = proto.getForall();
                yield new ForAllVar(strings[forall.getName()], (Kind) sorts[forall.getKind()]);
            }
            case EXISTS -> {
                var exists = proto.getExists();
                yield new ExistsVar(strings[exists.getName()], (Kind) sorts[exists.getKind()]);
            }
        };
    }
}

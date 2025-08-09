/*
 * SPDX-FileCopyrightText:  © 2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.proto;

import com.google.protobuf.Empty;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import org.mina_lang.common.Scope;
import org.mina_lang.common.names.NameVisitor;
import org.mina_lang.common.types.SortVisitor;
import org.mina_lang.common.types.SyntheticVar;
import org.mina_lang.proto.names.*;
import org.mina_lang.proto.types.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ProtobufWriter {
    public Environment toProto(Scope<org.mina_lang.common.Attributes> topLevelScope) {
        var builder = Environment.newBuilder();

        Object2IntMap<String> stringIndices = Object2IntMaps.emptyMap();
        List<String> strings = new ArrayList<>();

        Object2IntMap<org.mina_lang.common.names.Name> nameIndices = Object2IntMaps.emptyMap();
        Map<org.mina_lang.common.names.Name, Name> namesEncoded = new LinkedHashMap<>();
        var nameVisitor = new NameEncodingVisitor(stringIndices, strings, nameIndices, namesEncoded);

        Object2IntMap<org.mina_lang.common.types.Sort> sortIndices = Object2IntMaps.emptyMap();
        Map<org.mina_lang.common.types.Sort, Sort> sortsEncoded = new LinkedHashMap<>();
        var sortVisitor = new SortEncodingVisitor(stringIndices, strings, sortIndices, sortsEncoded, nameVisitor);

        topLevelScope.values().forEach((key, meta) -> {
            meta.meta().name().accept(nameVisitor);
            meta.meta().sort().accept(sortVisitor);
        });

        topLevelScope.types().forEach((key, meta) -> {
            meta.meta().name().accept(nameVisitor);
            meta.meta().sort().accept(sortVisitor);
        });

        topLevelScope.fields().forEach((constr, fields) -> {
            constr.accept(nameVisitor);

            fields.forEach((name, field) -> {
                field.meta().name().accept(nameVisitor);
                field.meta().sort().accept(sortVisitor);
            });
        });

        builder.addAllStrings(strings);
        builder.addAllNames(namesEncoded.values());
        builder.addAllSorts(sortsEncoded.values());

        topLevelScope.values().forEach((name, meta) -> {
            builder.putValues(stringIndices.get(name), Attributes.newBuilder()
                .setName(nameIndices.get(meta.meta().name()))
                .setSort(sortIndices.get(meta.meta().sort()))
                .build());
        });

        topLevelScope.types().forEach((name, meta) -> {
            builder.putTypes(stringIndices.get(name), Attributes.newBuilder()
                .setName(nameIndices.get(meta.meta().name()))
                .setSort(sortIndices.get(meta.meta().sort()))
                .build());
        });

        topLevelScope.fields().forEach((name, unencodedFields) -> {
            var fieldsBuilder = ConstructorFieldEntry.newBuilder()
                .setConstructor(nameIndices.get(name));

            unencodedFields.forEach((fieldName, fieldMeta) -> {
                fieldsBuilder.putFields(
                    stringIndices.get(fieldName), Attributes.newBuilder()
                    .setName(nameIndices.get(fieldMeta.meta().name()))
                    .setSort(sortIndices.get(fieldMeta.meta().sort()))
                    .build());
            });

            builder.addFields(fieldsBuilder);
        });

        return builder.build();
    }

    static class NameEncodingVisitor implements NameVisitor {
        private final Object2IntMap<String> stringIndices;
        private final List<String> strings;
        private final Object2IntMap<org.mina_lang.common.names.Name> nameIndices;
        private final Map<org.mina_lang.common.names.Name, Name> namesEncoded;

        NameEncodingVisitor(
            Object2IntMap<String> stringIndices,
            List<String> strings,
            Object2IntMap<org.mina_lang.common.names.Name> nameIndices,
            Map<org.mina_lang.common.names.Name, Name> namesEncoded) {
            this.stringIndices = stringIndices;
            this.strings = strings;
            this.nameIndices = nameIndices;
            this.namesEncoded = namesEncoded;
        }

        private int recordString(String string) {
            return stringIndices.computeIfAbsent(string, s -> {
                var index = strings.size();
                strings.add(string);
                return index;
            });
        }

        private void recordName(org.mina_lang.common.names.Name original, Supplier<Name> encoder) {
            if (!namesEncoded.containsKey(original)) {
                var encoded = encoder.get();
                nameIndices.put(original, namesEncoded.size());
                namesEncoded.put(original, encoded);
            }
        }

        public int getName(org.mina_lang.common.names.Name name) {
            return nameIndices.get(name);
        }

        @Override
        public void visitNameless(org.mina_lang.common.names.Nameless nameless) {
            // Nameless values should not appear in the top-level scope
        }

        @Override
        public void visitLocalName(org.mina_lang.common.names.LocalName local) {
            // Local names should not appear in the top-level scope
        }

        @Override
        public void visitSyntheticName(org.mina_lang.common.names.SyntheticName synthetic) {
            // Local names should not appear in the top-level scope
        }

        @Override
        public void visitBuiltInName(org.mina_lang.common.names.BuiltInName builtIn) {
            recordName(builtIn, () -> {
                var proto = BuiltInName.newBuilder()
                    .setName(recordString(builtIn.name()));
                return Name.newBuilder()
                    .setBuiltIn(proto)
                    .build();
            });
        }

        @Override
        public void visitNamespaceName(org.mina_lang.common.names.NamespaceName namespace) {
            recordName(namespace, () -> {
                var proto = NamespaceName.newBuilder()
                    .setName(recordString(namespace.name()))
                    .addAllPkg(namespace.pkg().stream().map(this::recordString).toList());
                return Name.newBuilder()
                    .setNamespace(proto)
                    .build();
            });
        }

        @Override
        public void visitFieldName(org.mina_lang.common.names.FieldName field) {
            field.constructor().accept(this);

            recordName(field, () -> {
                var proto = FieldName.newBuilder()
                    .setConstr(nameIndices.get(field.constructor()))
                    .setName(recordString(field.name()));
                return Name.newBuilder()
                    .setField(proto)
                    .build();
            });
        }

        @Override
        public void visitLetName(org.mina_lang.common.names.LetName let) {
            let.name().ns().accept(this);

            recordName(let, () -> {
                var proto = LetName.newBuilder()
                    .setName(QualifiedName.newBuilder()
                        .setNamespace(nameIndices.get(let.name().ns()))
                        .setName(recordString(let.name().name())));
                return Name.newBuilder()
                    .setLet(proto)
                    .build();
            });
        }

        @Override
        public void visitConstructorName(org.mina_lang.common.names.ConstructorName constructor) {
            constructor.name().ns().accept(this);
            constructor.enclosing().accept(this);

            recordName(constructor, () -> {
                var proto = ConstructorName.newBuilder()
                    .setEnclosing(nameIndices.get(constructor.enclosing()))
                    .setName(QualifiedName.newBuilder()
                        .setNamespace(nameIndices.get(constructor.name().ns()))
                        .setName(recordString(constructor.name().name())));
                return Name.newBuilder()
                    .setConstr(proto)
                    .build();
            });
        }

        @Override
        public void visitDataName(org.mina_lang.common.names.DataName data) {
            data.name().ns().accept(this);

            recordName(data, () -> {
                var proto = DataName.newBuilder()
                    .setName(QualifiedName.newBuilder()
                        .setNamespace(nameIndices.get(data.name().ns()))
                        .setName(recordString(data.name().name())));
                return Name.newBuilder()
                    .setData(proto)
                    .build();
            });
        }

        @Override
        public void visitExistsVarName(org.mina_lang.common.names.ExistsVarName existsVar) {
            recordName(existsVar, () -> {
                var proto = ExistsVarName.newBuilder()
                    .setName(recordString(existsVar.name()));
                return Name.newBuilder()
                    .setExists(proto)
                    .build();
            });
        }

        @Override
        public void visitForAllVarName(org.mina_lang.common.names.ForAllVarName forAllVar) {
            recordName(forAllVar, () -> {
                var proto = ForAllVarName.newBuilder()
                    .setName(recordString(forAllVar.name()));
                return Name.newBuilder()
                    .setForall(proto)
                    .build();
            });
        }
    }

    static class SortEncodingVisitor implements SortVisitor {
        private final Object2IntMap<String> stringIndices;
        private final List<String> strings;
        private final Object2IntMap<org.mina_lang.common.types.Sort> sortIndices;
        private final Map<org.mina_lang.common.types.Sort, Sort> sortsEncoded;
        private final NameEncodingVisitor nameVisitor;

        SortEncodingVisitor(
            Object2IntMap<String> stringIndices,
            List<String> strings,
            Object2IntMap<org.mina_lang.common.types.Sort> sortIndices,
            Map<org.mina_lang.common.types.Sort, Sort> sortsEncoded,
            NameEncodingVisitor nameVisitor
        ) {
            this.stringIndices = stringIndices;
            this.strings = strings;
            this.sortIndices = sortIndices;
            this.sortsEncoded = sortsEncoded;
            this.nameVisitor = nameVisitor;
        }

        private int recordString(String string) {
            return stringIndices.computeIfAbsent(string, s -> {
                var index = strings.size();
                strings.add(string);
                return index;
            });
        }

        private void recordSort(org.mina_lang.common.types.Sort original, Supplier<Sort> encoder) {
            if (!sortsEncoded.containsKey(original)) {
                var encoded = encoder.get();
                sortIndices.put(original, sortsEncoded.size());
                sortsEncoded.put(original, encoded);
            }
        }

        @Override
        public void visitTypeKind(org.mina_lang.common.types.TypeKind typ) {
            recordSort(typ, () -> {
                return Sort.newBuilder()
                    .setTypeKind(Empty.newBuilder())
                    .build();
            });
        }

        @Override
        public void visitUnsolvedKind(org.mina_lang.common.types.UnsolvedKind unsolved) {
            // We shouldn't have any unsolved kinds in the top-level scope
        }

        @Override
        public void visitUnsolvedType(org.mina_lang.common.types.UnsolvedType unsolved) {
            // We shouldn't have any unsolved types in the top-level scope
        }

        @Override
        public void visitSyntheticVar(SyntheticVar syn) {
            // We shouldn't have any synthetic vars in the top-level scope
        }

        @Override
        public void visitHigherKind(org.mina_lang.common.types.HigherKind higher) {
            higher.argKinds().forEach(arg -> arg.accept(this));
            higher.resultKind().accept(this);

            recordSort(higher, () -> {
                var proto = HigherKind.newBuilder()
                    .setResult(sortIndices.get(higher.resultKind()));

                higher.argKinds().forEach(arg -> {
                    proto.addArgs(sortIndices.get(arg));
                });

                return Sort.newBuilder()
                    .setHigherKind(proto)
                    .build();
            });
        }

        @Override
        public void visitQuantifiedType(org.mina_lang.common.types.QuantifiedType quant) {
            quant.args().forEach(arg -> arg.accept(this));
            quant.body().accept(this);
            quant.kind().accept(this);

            recordSort(quant, () -> {
                var proto = QuantifiedType.newBuilder()
                    .setBody(sortIndices.get(quant.body()))
                    .setKind(sortIndices.get(quant.kind()));

                quant.args().forEach(arg -> {
                    proto.addArgs(sortIndices.get(arg));
                });

                return Sort.newBuilder()
                    .setQuantTy(proto)
                    .build();
            });
        }

        @Override
        public void visitTypeConstructor(org.mina_lang.common.types.TypeConstructor tyCon) {
            tyCon.name().ns().accept(nameVisitor);
            tyCon.kind().accept(this);

            recordSort(tyCon, () -> {
                var proto = TypeConstructor.newBuilder()
                    .setName(QualifiedName.newBuilder()
                        .setNamespace(nameVisitor.getName(tyCon.name().ns()))
                        .setName(recordString(tyCon.name().name())))
                    .setKind(sortIndices.get(tyCon.kind()));

                return Sort.newBuilder()
                    .setTyCon(proto)
                    .build();
            });
        }

        @Override
        public void visitBuiltInType(org.mina_lang.common.types.BuiltInType builtIn) {
            builtIn.kind().accept(this);

            recordSort(builtIn, () -> {
                var proto = BuiltInType.newBuilder()
                    .setName(recordString(builtIn.name()))
                    .setKind(sortIndices.get(builtIn.kind()));

                return Sort.newBuilder()
                    .setBuiltIn(proto)
                    .build();
            });
        }

        @Override
        public void visitTypeApply(org.mina_lang.common.types.TypeApply tyApp) {
            tyApp.type().accept(this);
            tyApp.typeArguments().forEach(arg -> arg.accept(this));
            tyApp.kind().accept(this);

            recordSort(tyApp, () -> {
                var proto = TypeApply.newBuilder()
                    .setTyp(sortIndices.get(tyApp.type()))
                    .setKind(sortIndices.get(tyApp.kind()));

                tyApp.typeArguments().forEach(arg -> {
                    proto.addArgs(sortIndices.get(arg));
                });

                return Sort.newBuilder()
                    .setTyApp(proto)
                    .build();
            });
        }

        @Override
        public void visitForAllVar(org.mina_lang.common.types.ForAllVar forall) {
            forall.kind().accept(this);

            recordSort(forall, () -> {
                var proto = ForAllVar.newBuilder()
                    .setName(recordString(forall.name()))
                    .setKind(sortIndices.get(forall.kind()));

                return Sort.newBuilder()
                    .setForall(proto)
                    .build();
            });
        }

        @Override
        public void visitExistsVar(org.mina_lang.common.types.ExistsVar exists) {
            exists.kind().accept(this);

            recordSort(exists, () -> {
                var proto = ExistsVar.newBuilder()
                    .setName(recordString(exists.name()))
                    .setKind(sortIndices.get(exists.kind()));

                return Sort.newBuilder()
                    .setExists(proto)
                    .build();
            });
        }
    }
}

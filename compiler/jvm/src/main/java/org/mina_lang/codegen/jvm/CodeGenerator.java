package org.mina_lang.codegen.jvm;

import java.io.PrintWriter;
import java.util.Optional;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.TypeEnvironment;
import org.mina_lang.common.names.DataName;
import org.mina_lang.common.names.Name;
import org.mina_lang.common.names.Named;
import org.mina_lang.common.names.QualifiedName;
import org.mina_lang.common.scopes.Scope;
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.util.TraceClassVisitor;

import static org.objectweb.asm.Opcodes.*;

public class CodeGenerator {
    ClassWriter namespaceWriter;
    GeneratorAdapter namespaceInitWriter;

    ClassWriter dataWriter;

    ClassWriter constructorWriter;
    GeneratorAdapter constructorInitWriter;

    TypeEnvironment environment = TypeEnvironment.withBuiltInTypes();
    TypeAnnotationFolder typeFolder = new TypeAnnotationFolder(environment);

    MutableList<byte[]> classes = Lists.mutable.empty();

    DataNode<Attributes> currentData;
    ConstructorNode<Attributes> currentConstructor;

    public void generate(NamespaceNode<Attributes> namespace) {
        namespace.accept(new CodeGenerationFolder());

        for (byte[] classData : classes) {
            var reader = new ClassReader(classData);
            var writer = new PrintWriter(System.err);
            var visitor = new TraceClassVisitor(writer);
            reader.accept(visitor, 0);
        }
    }

    public GeneratorAdapter constructor(
            ClassWriter classWriter,
            String signature,
            Type... argTypes) {
        return new GeneratorAdapter(
                ACC_PUBLIC,
                new Method("<init>", Type.VOID_TYPE, argTypes),
                signature,
                null,
                classWriter);
    }

    public GeneratorAdapter staticInitializer(ClassWriter classWriter) {
        return new GeneratorAdapter(
                ACC_STATIC,
                new Method("<clinit>", Type.getMethodDescriptor(Type.VOID_TYPE)),
                null,
                null,
                classWriter);
    }

    String getDescriptor(QualifiedName name) {
        return "L" + getInternalName(name) + ";";
    }

    String getDescriptor(Named name) {
        return "L" + getInternalName(name) + ";";
    }

    String getDescriptor(MetaNode<Attributes> node) {
        var name = (Named) node.meta().meta().name();
        return getDescriptor(name);
    }

    String getInternalName(QualifiedName name) {
        return name.canonicalName().replaceAll("\\.", "/");
    }

    String getInternalName(Named name) {
        return name.canonicalName().replaceAll("\\.", "/");
    }

    String getInternalName(MetaNode<Attributes> node) {
        var name = (Named) node.meta().meta().name();
        return getInternalName(name);
    }

    Meta<Attributes> updateMetaWith(Meta<Attributes> meta, Sort sort) {
        var attributes = meta.meta().withSort(sort);
        return meta.withMeta(attributes);
    }

    void putTypeDeclaration(Scope<Attributes> scope, Meta<Attributes> meta) {
        var name = (Named) meta.meta().name();
        scope.putType(name.localName(), meta);
        scope.putType(name.canonicalName(), meta);
        return;
    }

    void putValueDeclaration(Scope<Attributes> scope, Meta<Attributes> meta) {
        var name = (Named) meta.meta().name();
        scope.putValue(name.localName(), meta);
        scope.putValue(name.canonicalName(), meta);
        return;
    }

    void putTypeDeclaration(Meta<Attributes> meta) {
        var name = (Named) meta.meta().name();
        environment.putType(name.localName(), meta);
        environment.putType(name.canonicalName(), meta);
        return;
    }

    void putValueDeclaration(Meta<Attributes> meta) {
        var name = (Named) meta.meta().name();
        environment.putValue(name.localName(), meta);
        environment.putValue(name.canonicalName(), meta);
        return;
    }

    org.mina_lang.common.types.Type createConstructorType(
            DataNode<Attributes> data,
            ConstructorNode<Attributes> constr) {
        var dataName = (DataName) data.meta().meta().name();
        var dataKind = getKind(data);

        var typeFolder = new TypeAnnotationFolder(environment);

        var typeParamTypes = data.typeParams()
                .collect(tyParam -> typeFolder.visitTypeVar(tyParam));

        var constrParamTypes = constr.params()
                .collect(param -> typeFolder.visitType(param.typeAnnotation()));

        var constrReturnType = constr.type()
                .map(typ -> typeFolder.visitType(typ))
                .orElseGet(() -> {
                    var tyCon = new TypeConstructor(dataName.name(), dataKind);
                    return typeParamTypes.isEmpty() ? tyCon
                            : new TypeApply(tyCon, typeParamTypes, TypeKind.INSTANCE);
                });

        var constrFnType = org.mina_lang.common.types.Type.function(constrParamTypes, constrReturnType);

        if (typeParamTypes.isEmpty()) {
            return constrFnType;
        } else {
            return new TypeLambda(
                    typeParamTypes.collect(tyParam -> (TypeVar) tyParam),
                    constrFnType,
                    dataKind);
        }
    }

    Kind getKind(MetaNode<Attributes> node) {
        return (Kind) node.meta().meta().sort();
    }

    org.mina_lang.common.types.Type getType(MetaNode<Attributes> node) {
        return (org.mina_lang.common.types.Type) node.meta().meta().sort();
    }

    Type asmType(org.mina_lang.common.types.Type minaType) {
        if (minaType.equals(org.mina_lang.common.types.Type.BOOLEAN)) {
            return Type.BOOLEAN_TYPE;
        } else if (minaType.equals(org.mina_lang.common.types.Type.CHAR)) {
            return Type.CHAR_TYPE;
        } else if (minaType.equals(org.mina_lang.common.types.Type.STRING)) {
            return Type.getType(String.class);
        } else if (minaType.equals(org.mina_lang.common.types.Type.INT)) {
            return Type.INT_TYPE;
        } else if (minaType.equals(org.mina_lang.common.types.Type.LONG)) {
            return Type.LONG_TYPE;
        } else if (minaType.equals(org.mina_lang.common.types.Type.FLOAT)) {
            return Type.FLOAT_TYPE;
        } else if (minaType.equals(org.mina_lang.common.types.Type.DOUBLE)) {
            return Type.DOUBLE_TYPE;
        } else if (minaType.equals(org.mina_lang.common.types.Type.UNIT)) {
            return Type.VOID_TYPE;
        } else if (org.mina_lang.common.types.Type.isFunction(minaType)) {
            // TODO: Create function classes
        } else if (minaType instanceof org.mina_lang.common.types.TypeVar tyVar) {
            return Type.getType(Object.class);
        } else if (minaType instanceof org.mina_lang.common.types.TypeApply tyApp) {
            return asmType(tyApp.type());
        } else if (minaType instanceof org.mina_lang.common.types.TypeConstructor tyCon) {
            return Type.getType(getDescriptor(tyCon.name()));
        } else if (minaType instanceof org.mina_lang.common.types.TypeLambda tyLam) {
            return asmType(tyLam.body());
        }

        return null;
    }

    Type asmType(MetaNode<Attributes> node) {
        var minaType = getType(node);
        return asmType(minaType);
    }

    class CodeGenerationFolder implements MetaNodeFolder<Attributes, Void> {
        @Override
        public void preVisitNamespace(NamespaceNode<Attributes> namespace) {
            namespaceWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            namespaceInitWriter = staticInitializer(namespaceWriter);

            namespaceWriter.visit(
                    V17,
                    ACC_PUBLIC + ACC_FINAL + ACC_SUPER,
                    getInternalName(namespace) + "/Namespace",
                    null,
                    Type.getInternalName(Object.class),
                    null);
        }

        @Override
        public Void visitNamespace(Meta<Attributes> meta, NamespaceIdNode id, ImmutableList<ImportNode> imports,
                ImmutableList<Void> declarations) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void postVisitNamespace(Void namespace) {
            namespaceInitWriter.visitEnd();
            namespaceWriter.visitEnd();
            classes.add(namespaceWriter.toByteArray());
        }

        // Data declarations
        @Override
        public void preVisitData(DataNode<Attributes> data) {
            currentData = data;

            dataWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

            putTypeDeclaration(data.meta());

            data.typeParams().forEach(tyParam -> {
                typeFolder.visitType(tyParam);
            });

            dataWriter.visit(
                    V17,
                    ACC_PUBLIC + ACC_INTERFACE + ACC_ABSTRACT,
                    getInternalName(data),
                    null,
                    Type.getInternalName(Object.class),
                    null);
        }

        @Override
        public Void visitData(Meta<Attributes> meta, String name, ImmutableList<Void> typeParams,
                ImmutableList<Void> constructors) {
            return null;
        }

        @Override
        public void postVisitData(Void data) {
            dataWriter.visitEnd();
            classes.add(dataWriter.toByteArray());
        }

        @Override
        public void preVisitConstructor(ConstructorNode<Attributes> constructor) {
            currentConstructor = constructor;
            constructorWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

            dataWriter.visitPermittedSubclass(getInternalName(constructor));

            putValueDeclaration(updateMetaWith(constructor.meta(), createConstructorType(currentData, constructor)));

            constructorWriter.visit(
                    V17,
                    ACC_PUBLIC + ACC_FINAL + ACC_SUPER,
                    getInternalName(constructor),
                    null,
                    Type.getInternalName(Record.class),
                    null);

            var fieldTypes = new Type[constructor.params().size()];

            constructorInitWriter = constructor(
                    constructorWriter,
                    null,
                    constructor.params()
                            .collect(ConstructorParamNode::typeAnnotation)
                            .collect(typeFolder::visitType)
                            .collect(CodeGenerator.this::asmType)
                            .toArray(fieldTypes));
        }

        @Override
        public Void visitConstructor(Meta<Attributes> meta, String name, ImmutableList<Void> params,
                Optional<Void> type) {
            return null;
        }

        @Override
        public void postVisitConstructor(Void constructor) {
            constructorInitWriter.visitEnd();
            constructorWriter.visitEnd();
            classes.add(constructorWriter.toByteArray());
        }

        @Override
        public void preVisitConstructorParam(ConstructorParamNode<Attributes> constrParam) {
            var paramType = typeFolder.visitType(constrParam.typeAnnotation());
            constructorWriter
                    .visitRecordComponent(constrParam.name(), asmType(paramType).getDescriptor(), null)
                    .visitEnd();
        }

        @Override
        public Void visitConstructorParam(Meta<Attributes> meta, String name, Void typeAnnotation) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitLet(Meta<Attributes> meta, String name, Optional<Void> type, Void expr) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitLetFn(Meta<Attributes> meta, String name, ImmutableList<Void> typeParams,
                ImmutableList<Void> valueParams, Optional<Void> returnType, Void expr) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitParam(Meta<Attributes> param, String name, Optional<Void> typeAnnotation) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitBlock(Meta<Attributes> meta, ImmutableList<Void> declarations, Optional<Void> result) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitIf(Meta<Attributes> meta, Void condition, Void consequent, Void alternative) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitLambda(Meta<Attributes> meta, ImmutableList<Void> params, Void body) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitMatch(Meta<Attributes> meta, Void scrutinee, ImmutableList<Void> cases) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitApply(Meta<Attributes> meta, Void expr, ImmutableList<Void> args) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitReference(Meta<Attributes> meta, QualifiedIdNode id) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitCase(Meta<Attributes> meta, Void pattern, Void consequent) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitAliasPattern(Meta<Attributes> meta, String alias, Void pattern) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitConstructorPattern(Meta<Attributes> meta, QualifiedIdNode id, ImmutableList<Void> fields) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitFieldPattern(Meta<Attributes> meta, String field, Optional<Void> pattern) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitIdPattern(Meta<Attributes> meta, String name) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitLiteralPattern(Meta<Attributes> meta, Void literal) {
            // TODO Auto-generated method stub
            return null;
        }

        // Literals
        @Override
        public Void visitBoolean(Meta<Attributes> meta, boolean value) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitChar(Meta<Attributes> meta, char value) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitString(Meta<Attributes> meta, String value) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitInt(Meta<Attributes> meta, int value) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitLong(Meta<Attributes> meta, long value) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitFloat(Meta<Attributes> meta, float value) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Void visitDouble(Meta<Attributes> meta, double value) {
            // TODO Auto-generated method stub
            return null;
        }

        // Types are part of our visitor contract, but we don't need to emit any code
        // for them
        @Override
        public Void visitTypeLambda(Meta<Attributes> meta, ImmutableList<Void> args, Void body) {
            return null;
        }

        @Override
        public Void visitFunType(Meta<Attributes> meta, ImmutableList<Void> argTypes, Void returnType) {
            return null;
        }

        @Override
        public Void visitTypeApply(Meta<Attributes> meta, Void type, ImmutableList<Void> args) {
            return null;
        }

        @Override
        public Void visitTypeReference(Meta<Attributes> meta, QualifiedIdNode id) {
            return null;
        }

        @Override
        public Void visitForAllVar(Meta<Attributes> meta, String name) {
            return null;
        }

        @Override
        public Void visitExistsVar(Meta<Attributes> meta, String name) {
            return null;
        }

    }
}

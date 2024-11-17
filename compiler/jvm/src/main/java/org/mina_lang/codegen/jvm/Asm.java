/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.LetName;
import org.mina_lang.common.types.TypeApply;
import org.mina_lang.common.types.TypeVar;
import org.mina_lang.syntax.ConstructorParamNode;
import org.mina_lang.syntax.ExprNode;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.lang.invoke.*;
import java.lang.runtime.ObjectMethods;

import static org.objectweb.asm.Opcodes.*;

public class Asm {
    private static String METAFACTORY_DESCRIPTOR = MethodType
            .methodType(
                    // Return type
                    CallSite.class,
                    // Stacked by the VM
                    MethodHandles.Lookup.class, // caller
                    String.class, // invokedName
                    MethodType.class, // invokedType
                    // Static arguments
                    MethodType.class, // samMethodType
                    MethodHandle.class, // implMethodType
                    MethodType.class // instantiatedMethodType
            ).toMethodDescriptorString();

    public static Handle METAFACTORY_HANDLE = new Handle(
            H_INVOKESTATIC,
            Type.getInternalName(LambdaMetafactory.class),
            "metafactory",
            METAFACTORY_DESCRIPTOR,
            false);

    private static String OBJECTMETHODS_DESCRIPTOR = MethodType
            .methodType(
                    // Return type
                    Object.class,
                    // Stacked by the VM
                    MethodHandles.Lookup.class, // caller
                    // Static arguments
                    String.class, // methodName
                    TypeDescriptor.class, // type
                    Class.class, // recordClass
                    String.class, // names
                    MethodHandle[].class // getters
            ).toMethodDescriptorString();

    public static Handle OBJECTMETHODS_HANDLE = new Handle(
            H_INVOKESTATIC,
            Type.getInternalName(ObjectMethods.class),
            "bootstrap",
            OBJECTMETHODS_DESCRIPTOR,
            false);

    public static GeneratorAdapter constructor(
            ClassWriter classWriter,
            String signature,
            ImmutableList<ConstructorParamNode<Attributes>> params) {
        var argTypes = params
                .collect(Types::asmType)
                .toArray(new Type[params.size()]);
        return new GeneratorAdapter(
                ACC_PUBLIC,
                new Method("<init>", Type.VOID_TYPE, argTypes),
                signature,
                null,
                classWriter);
    }

    public static GeneratorAdapter staticInitializer(ClassWriter classWriter) {
        return new GeneratorAdapter(
                ACC_STATIC,
                new Method("<clinit>", Type.getMethodDescriptor(Type.VOID_TYPE)),
                null,
                null,
                classWriter);
    }

    public static GeneratorAdapter methodWriter(
            int access,
            String name,
            Type returnType,
            ImmutableList<Type> argTypes,
            String signature,
            ClassWriter classWriter) {
        var argTypeArray = argTypes.toArray(new Type[argTypes.size()]);
        return new GeneratorAdapter(
                access,
                new Method(name, returnType, argTypeArray),
                signature,
                null,
                classWriter);
    }

    public static GeneratorAdapter methodWriter(
            int access,
            String name,
            ExprNode<Attributes> bodyExpr,
            ImmutableList<Type> argTypes,
            String signature,
            ClassWriter classWriter) {
        var argTypeArray = argTypes.toArray(new Type[argTypes.size()]);
        var returnType = Types.asmType(bodyExpr);
        return new GeneratorAdapter(
                access,
                new Method(name, returnType, argTypeArray),
                signature,
                null,
                classWriter);
    }

    public static GeneratorAdapter methodWriter(
            String name,
            Type returnType,
            ImmutableList<Type> argTypes,
            String signature,
            ClassWriter classWriter) {
        var argTypeArray = argTypes.toArray(new Type[argTypes.size()]);
        return new GeneratorAdapter(
                ACC_PUBLIC + ACC_STATIC,
                new Method(name, returnType, argTypeArray),
                signature,
                null,
                classWriter);
    }

    public static GeneratorAdapter methodWriter(
            String name,
            ExprNode<Attributes> bodyExpr,
            ImmutableList<Type> argTypes,
            String signature,
            ClassWriter classWriter) {
        var argTypeArray = argTypes.toArray(new Type[argTypes.size()]);
        var returnType = Types.asmType(bodyExpr);
        return new GeneratorAdapter(
                ACC_PUBLIC + ACC_STATIC,
                new Method(name, returnType, argTypeArray),
                signature,
                null,
                classWriter);
    }

    public static void emitStaticField(
            ClassWriter classWriter,
            String fieldName,
            Type fieldType,
            String signature,
            Object initialValue) {
        classWriter.visitField(
                ACC_PUBLIC + ACC_STATIC + ACC_FINAL,
                fieldName, fieldType.getDescriptor(), signature, initialValue);
    }

    public static void emitConstructorField(
            ClassWriter classWriter,
            GeneratorAdapter initWriter,
            Type constrType,
            int fieldIndex,
            String fieldName,
            Type fieldType,
            String signature) {
        classWriter.visitField(
                ACC_PRIVATE + ACC_FINAL,
                fieldName,
                fieldType.getDescriptor(),
                signature,
                null).visitEnd();

        initWriter.loadThis();
        initWriter.loadArg(fieldIndex);
        initWriter.putField(
                constrType,
                fieldName,
                fieldType);
    }

    public static void emitFieldGetter(
            ClassWriter classWriter,
            Type constrType,
            String fieldName,
            Type fieldType,
            String thisSignature,
            String getterSignature) {
        var getterVisitor = new GeneratorAdapter(
                ACC_PUBLIC,
                new Method(fieldName, Type.getMethodDescriptor(fieldType)),
                getterSignature,
                null,
                classWriter);

        var getterStartLabel = new Label();
        var getterEndLabel = new Label();

        getterVisitor.visitLabel(getterStartLabel);

        getterVisitor.loadThis();
        getterVisitor.getField(constrType, fieldName, fieldType);
        getterVisitor.returnValue();

        getterVisitor.visitLabel(getterEndLabel);

        getterVisitor.visitLocalVariable(
                "this",
                constrType.getDescriptor(),
                thisSignature,
                getterStartLabel,
                getterEndLabel,
                0);

        getterVisitor.endMethod();
    }

    public static void emitObjectBootstrapMethod(
            String methodName,
            Type returnType,
            ImmutableList<Type> argTypes,
            ClassWriter classWriter,
            Type constrType,
            String thisSignature,
            String methodSignature,
            ImmutableList<ConstructorParamNode<Attributes>> constrParams) {

        var methodDescriptor = Type.getMethodDescriptor(
                returnType,
                argTypes.toArray(new Type[argTypes.size()]));

        var methodVisitor = new GeneratorAdapter(
                ACC_PUBLIC,
                new Method(methodName, methodDescriptor),
                methodSignature,
                null,
                classWriter);

        var startLabel = new Label();
        var endLabel = new Label();

        methodVisitor.visitLabel(startLabel);

        methodVisitor.loadThis();

        if (methodName.equals("equals")) {
            methodVisitor.loadArg(0);
        }

        var callsiteDescriptor = Type.getMethodDescriptor(
                returnType,
                Lists.immutable.of(constrType)
                        .newWithAll(argTypes)
                        .toArray(new Type[argTypes.size() + 1]));

        methodVisitor.invokeDynamic(
                methodName, // methodName
                callsiteDescriptor, // type
                OBJECTMETHODS_HANDLE, // bootstrapMethodHandle
                Lists.immutable.<Object>of(
                        constrType.getInternalName(), // recordClass
                        constrParams.collect(ConstructorParamNode::name).makeString(";")) // names
                        .newWithAll(
                                constrParams.collect(param -> {
                                    return new Handle(
                                            H_GETFIELD,
                                            constrType.getInternalName(),
                                            param.name(),
                                            Types.asmType(param).getDescriptor(),
                                            false); // getters
                                }))
                        .toArray());

        methodVisitor.returnValue();

        methodVisitor.visitLabel(endLabel);

        methodVisitor.visitLocalVariable(
                "this",
                constrType.getDescriptor(),
                thisSignature,
                startLabel,
                endLabel,
                0);

        if (methodName.equals("equals")) {
            methodVisitor.visitLocalVariable(
                    "other",
                    Types.OBJECT_TYPE.getDescriptor(),
                    thisSignature,
                    startLabel,
                    endLabel,
                    1);
        }

        methodVisitor.endMethod();
    }

    public static Handle staticMethodHandle(LetName letName, org.mina_lang.common.types.Type letType) {
        var funType = (TypeApply) Types.getUnderlyingType(letType);
        var funReturnType = Types.asmType(funType.typeArguments().getLast());
        var funArgTypes = funType.typeArguments()
                .take(funType.typeArguments().size() - 1)
                .collect(Types::asmType)
                .toArray(new Type[funType.typeArguments().size() - 1]);

        return new Handle(
                H_INVOKESTATIC,
                Names.getInternalName(letName.name().ns()),
                letName.name().name(),
                Type.getMethodDescriptor(funReturnType, funArgTypes),
                false);
    }

    public static Handle constructorMethodHandle(ConstructorName constrName,
            org.mina_lang.common.types.Type constrType) {
        var funType = (TypeApply) Types.getUnderlyingType(constrType);
        var funArgTypes = funType.typeArguments()
                .take(funType.typeArguments().size() - 1)
                .collect(Types::asmType)
                .toArray(new Type[funType.typeArguments().size() - 1]);

        return new Handle(
                H_NEWINVOKESPECIAL,
                Names.getInternalName(constrName),
                "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE, funArgTypes),
                false);
    }

    public static void boxUnboxArgExpr(
            GeneratorAdapter methodWriter,
            org.mina_lang.common.types.Type interfaceType,
            org.mina_lang.common.types.Type implType) {
        if (!interfaceType.isPrimitive() && implType.isPrimitive()) {
            methodWriter.box(Types.asmType(implType));
        } else if (interfaceType.isPrimitive() && !implType.isPrimitive()) {
            methodWriter.unbox(Types.asmType(interfaceType));
        }
    }

    public static void unboxReturnValue(
            GeneratorAdapter methodWriter,
            org.mina_lang.common.types.Type implType) {
        if (implType.isPrimitive()) {
            methodWriter.unbox(Types.asmType(implType));
        } else {
            methodWriter.checkCast(Types.asmType(implType));
        }
    }

    public static void boxUnboxReturnValue(
            GeneratorAdapter methodWriter,
            org.mina_lang.common.types.Type interfaceType,
            org.mina_lang.common.types.Type implType) {
        if (interfaceType.isPrimitive() && !implType.isPrimitive()) {
            methodWriter.box(Types.asmType(interfaceType));
        } else if (!interfaceType.isPrimitive() && implType.isPrimitive()) {
            methodWriter.unbox(Types.asmType(implType));
        } else if (interfaceType instanceof TypeVar tyVar) {
            methodWriter.checkCast(Types.asmType(implType));
        }
    }

    public static class EnvironmentAttribute extends Attribute {
        private final byte[] data;

        public EnvironmentAttribute(byte[] data) {
            super("MinaEnvironment");
            this.data = data;
        }

        public byte[] data() {
            return data;
        }

        @Override
        protected Attribute read(ClassReader classReader, int offset, int length, char[] charBuffer, int codeAttributeOffset, Label[] labels) {
            return new EnvironmentAttribute(classReader.readBytes(offset, length));
        }

        @Override
        protected ByteVector write(ClassWriter classWriter, byte[] code, int codeLength, int maxStack, int maxLocals) {
            var byteVector = new ByteVector(data.length);
            byteVector.putByteArray(data, 0, data.length);
            return byteVector;
        }
    }

    public static class EnvironmentAttributeVisitor extends ClassVisitor {
        private byte[] data = new byte[0];

        protected EnvironmentAttributeVisitor() {
            super(ASM9);
        }

        public byte[] data() {
            return data;
        }

        @Override
        public void visitAttribute(Attribute attribute) {
            if (attribute instanceof EnvironmentAttribute env) {
                data = env.data();
            }
        }
    }
}

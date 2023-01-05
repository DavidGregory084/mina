package org.mina_lang.codegen.jvm;

import static org.objectweb.asm.Opcodes.*;

import java.lang.invoke.*;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.LetName;
import org.mina_lang.common.types.TypeApply;
import org.mina_lang.common.types.TypeVar;
import org.mina_lang.syntax.ConstructorParamNode;
import org.mina_lang.syntax.ExprNode;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

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
                ACC_STATIC,
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
                ACC_STATIC,
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

        getterVisitor.visitEnd();
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
        org.mina_lang.common.types.Type implType
    ) {
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
}

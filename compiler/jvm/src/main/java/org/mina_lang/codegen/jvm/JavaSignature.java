package org.mina_lang.codegen.jvm;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.types.TypeApply;
import org.mina_lang.common.types.TypeLambda;
import org.mina_lang.common.types.TypeVar;
import org.mina_lang.common.types.UnsolvedType;
import org.mina_lang.syntax.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

public class JavaSignature {
    private static String OBJECT_NAME = Type.getInternalName(Object.class);
    private static String RECORD_NAME = Type.getInternalName(Record.class);

    public static String forData(DataNode<Attributes> data) {
        var visitor = new SignatureWriter();

        data.typeParams().forEach(tyParam -> {
            visitor.visitFormalTypeParameter(tyParam.name());
            var boundVisitor = visitor.visitClassBound();
            boundVisitor.visitClassType(OBJECT_NAME);
            boundVisitor.visitEnd();
        });

        var superClassVisitor = visitor.visitSuperclass();
        superClassVisitor.visitClassType(OBJECT_NAME);
        superClassVisitor.visitEnd();

        return visitor.toString();
    }

    public static String forConstructor(DataNode<Attributes> data, ConstructorNode<Attributes> constr) {
        var visitor = new SignatureWriter();

        var constrType = Types.getType(constr);

        if (constrType instanceof TypeLambda tyLam) {
            var funType = (TypeApply) Types.getUnderlyingType(constr);
            var returnType = (TypeApply) funType.typeArguments().getLast();
            returnType.typeArguments().forEach(tyArg -> {
                if (tyArg instanceof TypeVar tyVar) {
                    visitor.visitFormalTypeParameter(tyVar.name());
                    var boundVisitor = visitor.visitClassBound();
                    boundVisitor.visitClassType(OBJECT_NAME);
                    boundVisitor.visitEnd();
                }
            });
        }

        var superclassVisitor = visitor.visitSuperclass();
        superclassVisitor.visitClassType(RECORD_NAME);
        superclassVisitor.visitEnd();

        var interfaceVisitor = visitor.visitInterface();
        interfaceVisitor.visitClassType(Names.getInternalName(data));

        if (constrType instanceof TypeLambda tyLam) {
            var funType = (TypeApply) Types.getUnderlyingType(constr);
            var returnType = (TypeApply) funType.typeArguments().getLast();
            returnType.typeArguments().forEach(returnTyArg -> {
                var argVisitor = interfaceVisitor.visitTypeArgument(SignatureVisitor.INSTANCEOF);
                writeBoxedType(argVisitor, returnTyArg);
            });
        }

        interfaceVisitor.visitEnd();

        return visitor.toString();
    }

    public static String forJavaConstructor(ConstructorNode<Attributes> constr) {
        var visitor = new SignatureWriter();

        var constrType = Types.getType(constr);
        var funType = (TypeApply) Types.getUnderlyingType(constr);

        if (constrType instanceof TypeLambda tyLam) {
            var returnType = (TypeApply) funType.typeArguments().getLast();
            returnType.typeArguments().forEach(tyArg -> {
                if (tyArg instanceof TypeVar tyVar) {
                    visitor.visitFormalTypeParameter(tyVar.name());
                    var boundVisitor = visitor.visitClassBound();
                    boundVisitor.visitClassType(OBJECT_NAME);
                    boundVisitor.visitEnd();
                }
            });
        }

        funType.typeArguments()
                .take(funType.typeArguments().size() - 1)
                .forEach(paramType -> {
                    var paramVisitor = visitor.visitParameterType();
                    writeType(paramVisitor, paramType);
                });

        var returnTypeVisitor = visitor.visitReturnType();
        returnTypeVisitor.visitBaseType(Type.VOID_TYPE.getDescriptor().charAt(0));

        return visitor.toString();
    }

    public static String forConstructorInstance(ConstructorNode<Attributes> constr) {
        var visitor = new SignatureWriter();

        var constrType = Types.getType(constr);

        visitor.visitClassType(Types.getConstructorAsmType(constr).getInternalName());

        if (constrType instanceof TypeLambda tyLam) {
            var funType = (TypeApply) Types.getUnderlyingType(constr);
            var returnType = (TypeApply) funType.typeArguments().getLast();
            returnType.typeArguments().forEach(returnTyArg -> {
                if (returnTyArg instanceof TypeVar tyVar) {
                    var argVisitor = visitor.visitTypeArgument(SignatureVisitor.INSTANCEOF);
                    argVisitor.visitTypeVariable(tyVar.name());
                }
            });
        }

        visitor.visitEnd();

        return visitor.toString();
    }

    public static String forFieldGetter(org.mina_lang.common.types.Type type) {
        var visitor = new SignatureWriter();
        var returnTypeVisitor = visitor.visitReturnType();
        writeType(returnTypeVisitor, type);
        return visitor.toString();
    }

    public static String forMethod(MetaNode<Attributes> node) {
        var visitor = new SignatureWriter();
        var type = Types.getType(node);

        if (type instanceof TypeLambda tyLam) {
            tyLam.args().forEach(tyArg -> {
                visitor.visitFormalTypeParameter(tyArg.name());
                var boundVisitor = visitor.visitClassBound();
                boundVisitor.visitClassType(OBJECT_NAME);
                boundVisitor.visitEnd();
            });
        }

        var funType = (TypeApply) Types.getUnderlyingType(type);

        funType.typeArguments()
                .take(funType.typeArguments().size() - 1)
                .forEach(paramType -> {
                    var paramVisitor = visitor.visitParameterType();
                    writeType(paramVisitor, paramType);
                });

        var returnTypeVisitor = visitor.visitReturnType();
        writeType(returnTypeVisitor, funType.typeArguments().getLast());

        return visitor.toString();
    }

    public static String forType(org.mina_lang.common.types.Type type) {
        if (type.isPrimitive()) {
            return null;
        } else {
            var visitor = new SignatureWriter();
            writeType(visitor, type);
            return visitor.toString();
        }
    }

    public static void writeType(
            SignatureVisitor visitor,
            org.mina_lang.common.types.Type type) {
        if (type.equals(org.mina_lang.common.types.Type.BOOLEAN)) {
            visitor.visitBaseType(Type.BOOLEAN_TYPE.getDescriptor().charAt(0));
        } else if (type.equals(org.mina_lang.common.types.Type.CHAR)) {
            visitor.visitBaseType(Type.CHAR_TYPE.getDescriptor().charAt(0));
        } else if (type.equals(org.mina_lang.common.types.Type.STRING)) {
            visitor.visitClassType(Types.STRING_TYPE.getInternalName());
            visitor.visitEnd();
        } else if (type.equals(org.mina_lang.common.types.Type.INT)) {
            visitor.visitBaseType(Type.INT_TYPE.getDescriptor().charAt(0));
        } else if (type.equals(org.mina_lang.common.types.Type.LONG)) {
            visitor.visitBaseType(Type.LONG_TYPE.getDescriptor().charAt(0));
        } else if (type.equals(org.mina_lang.common.types.Type.FLOAT)) {
            visitor.visitBaseType(Type.FLOAT_TYPE.getDescriptor().charAt(0));
        } else if (type.equals(org.mina_lang.common.types.Type.DOUBLE)) {
            visitor.visitBaseType(Type.DOUBLE_TYPE.getDescriptor().charAt(0));
        } else if (type.equals(org.mina_lang.common.types.Type.UNIT)) {
            visitor.visitClassType(Types.UNIT_TYPE.getInternalName());
            visitor.visitEnd();
        } else if (type instanceof org.mina_lang.common.types.TypeApply tyApp
                && tyApp.type() instanceof TypeVar tyVar) {
            writeType(visitor, tyVar);
        } else if (type instanceof org.mina_lang.common.types.TypeApply tyApp
                && tyApp.type() instanceof UnsolvedType unsolved) {
            writeType(visitor, unsolved);
        } else if (type instanceof org.mina_lang.common.types.TypeApply tyApp &&
                org.mina_lang.common.types.Type.isFunction(type)) {
            visitor.visitClassType(Types.asmType(tyApp).getInternalName());

            tyApp.typeArguments().forEach(tyArg -> {
                var argVisitor = visitor.visitTypeArgument(SignatureVisitor.INSTANCEOF);
                writeBoxedType(argVisitor, tyArg);
            });

            visitor.visitEnd();
        } else if (type instanceof org.mina_lang.common.types.TypeApply tyApp) {
            visitor.visitClassType(Types.asmType(tyApp.type()).getInternalName());

            tyApp.typeArguments().forEach(tyArg -> {
                var argVisitor = visitor.visitTypeArgument(SignatureVisitor.INSTANCEOF);
                writeBoxedType(argVisitor, tyArg);
            });

            visitor.visitEnd();
        } else if (type instanceof org.mina_lang.common.types.TypeVar tyVar) {
            visitor.visitTypeVariable(tyVar.name());
        } else if (type instanceof org.mina_lang.common.types.TypeConstructor tyCon) {
            visitor.visitClassType(Types.asmType(tyCon).getInternalName());
            visitor.visitEnd();
        } else if (type instanceof org.mina_lang.common.types.TypeLambda tyLam) {
            writeType(visitor, tyLam.body());
        } else if (type instanceof org.mina_lang.common.types.UnsolvedType unsolved) {
            visitor.visitTypeVariable(unsolved.name());
        }
    }

    public static void writeBoxedType(
            SignatureVisitor visitor,
            org.mina_lang.common.types.Type type) {
        if (type.equals(org.mina_lang.common.types.Type.BOOLEAN)) {
            visitor.visitClassType(Types.BOXED_BOOLEAN_TYPE.getInternalName());
            visitor.visitEnd();
        } else if (type.equals(org.mina_lang.common.types.Type.CHAR)) {
            visitor.visitClassType(Types.BOXED_CHAR_TYPE.getInternalName());
            visitor.visitEnd();
        } else if (type.equals(org.mina_lang.common.types.Type.INT)) {
            visitor.visitClassType(Types.BOXED_INT_TYPE.getInternalName());
            visitor.visitEnd();
        } else if (type.equals(org.mina_lang.common.types.Type.LONG)) {
            visitor.visitClassType(Types.BOXED_LONG_TYPE.getInternalName());
            visitor.visitEnd();
        } else if (type.equals(org.mina_lang.common.types.Type.FLOAT)) {
            visitor.visitClassType(Types.BOXED_FLOAT_TYPE.getInternalName());
            visitor.visitEnd();
        } else if (type.equals(org.mina_lang.common.types.Type.DOUBLE)) {
            visitor.visitClassType(Types.BOXED_DOUBLE_TYPE.getInternalName());
            visitor.visitEnd();
        } else {
            writeType(visitor, type);
        }
    }
}

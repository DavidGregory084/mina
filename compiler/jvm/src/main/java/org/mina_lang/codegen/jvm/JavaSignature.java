/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm;

import org.mina_lang.common.Attributes;
import org.mina_lang.common.types.*;
import org.mina_lang.syntax.ConstructorNode;
import org.mina_lang.syntax.DataNode;
import org.mina_lang.syntax.MetaNode;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

import java.util.HashSet;
import java.util.Set;

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

        if (constrType instanceof QuantifiedType quant) {
            var funType = (TypeApply) Types.getUnderlyingType(constr);
            var returnType = (TypeApply) funType.typeArguments().get(funType.typeArguments().size() - 1);
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

        if (constrType instanceof QuantifiedType quant) {
            var funType = (TypeApply) Types.getUnderlyingType(constr);
            var returnType = (TypeApply) funType.typeArguments().get(funType.typeArguments().size() - 1);
            returnType.typeArguments().forEach(returnTyArg -> {
                var argVisitor = interfaceVisitor.visitTypeArgument(SignatureVisitor.INSTANCEOF);
                returnTyArg.accept(new BoxedTypeSignatureVisitor(argVisitor));
            });
        }

        interfaceVisitor.visitEnd();

        return visitor.toString();
    }

    public static String forJavaConstructor(ConstructorNode<Attributes> constr) {
        var visitor = new SignatureWriter();

        var constrType = Types.getType(constr);
        var funType = (TypeApply) Types.getUnderlyingType(constr);

        if (constrType instanceof QuantifiedType quant) {
            var returnType = (TypeApply) funType.typeArguments().get(funType.typeArguments().size() - 1);
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
            .subList(0, funType.typeArguments().size() - 1)
            .forEach(paramType -> {
                var paramVisitor = visitor.visitParameterType();
                paramType.accept(new TypeSignatureVisitor(paramVisitor));
            });

        var returnTypeVisitor = visitor.visitReturnType();
        returnTypeVisitor.visitBaseType(Type.VOID_TYPE.getDescriptor().charAt(0));

        return visitor.toString();
    }

    public static String forConstructorInstance(ConstructorNode<Attributes> constr) {
        var visitor = new SignatureWriter();

        var constrType = Types.getType(constr);

        visitor.visitClassType(Types.getConstructorAsmType(constr).getInternalName());

        if (constrType instanceof QuantifiedType quant) {
            var funType = (TypeApply) Types.getUnderlyingType(constr);
            var returnType = (TypeApply) funType.typeArguments().get(funType.typeArguments().size() - 1);
            returnType.typeArguments().forEach(returnTyArg -> {
                if (returnTyArg instanceof ForAllVar forall) {
                    var argVisitor = visitor.visitTypeArgument(SignatureVisitor.INSTANCEOF);
                    argVisitor.visitTypeVariable(forall.name());
                } else if (returnTyArg instanceof ExistsVar exists) {
                    visitor.visitTypeArgument();
                }
            });
        }

        visitor.visitEnd();

        return visitor.toString();
    }

    public static String forFieldGetter(org.mina_lang.common.types.Type type) {
        var visitor = new SignatureWriter();
        var returnTypeVisitor = visitor.visitReturnType();
        type.accept(new TypeSignatureVisitor(returnTypeVisitor));
        return visitor.toString();
    }

    public static String forMethod(MetaNode<Attributes> node) {
        var visitor = new SignatureWriter();
        var type = Types.getType(node);

        if (type instanceof QuantifiedType quant) {
            quant.args().forEach(tyArg -> {
                visitor.visitFormalTypeParameter(tyArg.name());
                var boundVisitor = visitor.visitClassBound();
                boundVisitor.visitClassType(OBJECT_NAME);
                boundVisitor.visitEnd();
            });
        }

        var funType = (TypeApply) Types.getUnderlyingType(type);

        funType.typeArguments()
            .subList(0, funType.typeArguments().size() - 1)
            .forEach(paramType -> {
                var paramVisitor = visitor.visitParameterType();
                paramType.accept(new TypeSignatureVisitor(paramVisitor));
            });

        var returnTypeVisitor = visitor.visitReturnType();
        funType.typeArguments().get(funType.typeArguments().size() - 1).accept(new TypeSignatureVisitor(returnTypeVisitor));

        return visitor.toString();
    }

    public static String forType(org.mina_lang.common.types.Type type) {
        if (type.isPrimitive()) {
            return null;
        } else {
            var visitor = new SignatureWriter();
            type.accept(new TypeSignatureVisitor(visitor));
            return visitor.toString();
        }
    }

    public static class TypeSignatureVisitor implements TypeVisitor {
        protected Set<TypeVar> boundVars = new HashSet<>();
        protected SignatureVisitor visitor;

        public TypeSignatureVisitor(SignatureVisitor visitor) {
            this.visitor = visitor;
        }

        @Override
        public void visitQuantifiedType(QuantifiedType quant) {
            boundVars.addAll(quant.args());
            quant.body().accept(this);
            quant.args().forEach(boundVars::remove);
        }

        @Override
        public void visitTypeConstructor(TypeConstructor tyCon) {
            var asmType = Types.asmType(tyCon);
            visitor.visitClassType(asmType.getInternalName());
            visitor.visitEnd();
        }

        @Override
        public void visitBuiltInType(BuiltInType builtIn) {
            if (builtIn.equals(org.mina_lang.common.types.Type.BOOLEAN)) {
                visitor.visitBaseType(Type.BOOLEAN_TYPE.getDescriptor().charAt(0));
            } else if (builtIn.equals(org.mina_lang.common.types.Type.CHAR)) {
                visitor.visitBaseType(Type.CHAR_TYPE.getDescriptor().charAt(0));
            } else if (builtIn.equals(org.mina_lang.common.types.Type.STRING)) {
                visitor.visitClassType(Types.STRING_TYPE.getInternalName());
                visitor.visitEnd();
            } else if (builtIn.equals(org.mina_lang.common.types.Type.INT)) {
                visitor.visitBaseType(Type.INT_TYPE.getDescriptor().charAt(0));
            } else if (builtIn.equals(org.mina_lang.common.types.Type.LONG)) {
                visitor.visitBaseType(Type.LONG_TYPE.getDescriptor().charAt(0));
            } else if (builtIn.equals(org.mina_lang.common.types.Type.FLOAT)) {
                visitor.visitBaseType(Type.FLOAT_TYPE.getDescriptor().charAt(0));
            } else if (builtIn.equals(org.mina_lang.common.types.Type.DOUBLE)) {
                visitor.visitBaseType(Type.DOUBLE_TYPE.getDescriptor().charAt(0));
            } else if (builtIn.equals(org.mina_lang.common.types.Type.UNIT)) {
                visitor.visitClassType(Types.UNIT_TYPE.getInternalName());
                visitor.visitEnd();
            }
        }

        @Override
        public void visitTypeApply(TypeApply tyApp) {
            if (tyApp.type() instanceof TypeVar tyVar) {
                // Higher-kinded type variables can't be represented in Java signatures
                tyVar.accept(this);
            } else {
                if (org.mina_lang.common.types.Type.isFunction(tyApp)) {
                    visitor.visitClassType(Types.asmType(tyApp).getInternalName());
                } else {
                    visitor.visitClassType(Types.asmType(tyApp.type()).getInternalName());
                }

                tyApp.typeArguments().forEach(tyArg -> {
                    if (tyArg instanceof ExistsVar) {
                        // We use unbounded wildcard to represent existentials
                        visitor.visitTypeArgument();
                    } else if (boundVars.contains(tyArg)) {
                        // Higher-rank types can't be represented in Java signatures;
                        // we use unbounded wildcard to represent these too
                        visitor.visitTypeArgument();
                    } else {
                        var argVisitor = visitor.visitTypeArgument(SignatureVisitor.INSTANCEOF);
                        tyArg.accept(new BoxedTypeSignatureVisitor(argVisitor));
                    }

                });

                visitor.visitEnd();
            }
        }

        @Override
        public void visitForAllVar(ForAllVar forall) {
            visitor.visitTypeVariable(forall.name());
        }

        @Override
        public void visitExistsVar(ExistsVar exists) {
            visitor.visitTypeVariable(exists.name());
        }

        @Override
        public void visitSyntheticVar(SyntheticVar syn) {
            visitor.visitTypeVariable(syn.name());
        }

        @Override
        public void visitUnsolvedType(UnsolvedType unsolved) {
            visitor.visitTypeVariable(unsolved.name());
        }

    }

    public static class BoxedTypeSignatureVisitor extends TypeSignatureVisitor {

        public BoxedTypeSignatureVisitor(SignatureVisitor visitor) {
            super(visitor);
        }

        @Override
        public void visitBuiltInType(BuiltInType builtIn) {
            if (builtIn.equals(org.mina_lang.common.types.Type.BOOLEAN)) {
                visitor.visitClassType(Types.BOXED_BOOLEAN_TYPE.getInternalName());
                visitor.visitEnd();
            } else if (builtIn.equals(org.mina_lang.common.types.Type.CHAR)) {
                visitor.visitClassType(Types.BOXED_CHAR_TYPE.getInternalName());
                visitor.visitEnd();
            } else if (builtIn.equals(org.mina_lang.common.types.Type.STRING)) {
                visitor.visitClassType(Types.STRING_TYPE.getInternalName());
                visitor.visitEnd();
            } else if (builtIn.equals(org.mina_lang.common.types.Type.INT)) {
                visitor.visitClassType(Types.BOXED_INT_TYPE.getInternalName());
                visitor.visitEnd();
            } else if (builtIn.equals(org.mina_lang.common.types.Type.LONG)) {
                visitor.visitClassType(Types.BOXED_LONG_TYPE.getInternalName());
                visitor.visitEnd();
            } else if (builtIn.equals(org.mina_lang.common.types.Type.FLOAT)) {
                visitor.visitClassType(Types.BOXED_FLOAT_TYPE.getInternalName());
                visitor.visitEnd();
            } else if (builtIn.equals(org.mina_lang.common.types.Type.DOUBLE)) {
                visitor.visitClassType(Types.BOXED_DOUBLE_TYPE.getInternalName());
                visitor.visitEnd();
            } else if (builtIn.equals(org.mina_lang.common.types.Type.UNIT)) {
                visitor.visitClassType(Types.UNIT_TYPE.getInternalName());
                visitor.visitEnd();
            }
        }
    }
}

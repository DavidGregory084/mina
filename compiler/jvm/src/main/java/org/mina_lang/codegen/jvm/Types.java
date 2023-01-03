package org.mina_lang.codegen.jvm;

import java.util.Arrays;

import org.eclipse.collections.api.list.ImmutableList;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.DataName;
import org.mina_lang.common.names.NamespaceName;
import org.mina_lang.common.types.Kind;
import org.mina_lang.common.types.TypeApply;
import org.mina_lang.common.types.TypeLambda;
import org.mina_lang.syntax.ConstructorNode;
import org.mina_lang.syntax.DataNode;
import org.mina_lang.syntax.MetaNode;
import org.mina_lang.syntax.NamespaceNode;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

public class Types {
    private static Type OBJECT_TYPE = Type.getType(Object.class);

    public static Kind getKind(MetaNode<Attributes> node) {
        return (Kind) node.meta().meta().sort();
    }

    public static Type getNamespaceAsmType(NamespaceName nsName) {
        return Type.getType(Names.getDescriptor(nsName));
    }

    public static Type getNamespaceAsmType(NamespaceNode<Attributes> namespace) {
        return Type.getType(Names.getDescriptor(namespace));
    }

    public static Type getDataAsmType(DataName dataName) {
        return Type.getType(Names.getDescriptor(dataName));
    }

    public static Type getDataAsmType(DataNode<Attributes> data) {
        return Type.getType(Names.getDescriptor(data));
    }

    public static Type getConstructorAsmType(ConstructorName constrName) {
        return Type.getType(Names.getDescriptor(constrName));
    }

    public static Type getConstructorAsmType(ConstructorNode<Attributes> constr) {
        return Type.getType(Names.getDescriptor(constr));
    }

    public static org.mina_lang.common.types.Type getType(MetaNode<Attributes> node) {
        return (org.mina_lang.common.types.Type) node.meta().meta().sort();
    }

    public static org.mina_lang.common.types.Type getUnderlyingType(MetaNode<Attributes> node) {
        var type = (org.mina_lang.common.types.Type) node.meta().meta().sort();
        return getUnderlyingType(type);
    }

    public static org.mina_lang.common.types.Type getUnderlyingType(org.mina_lang.common.types.Type type) {
        while (type instanceof TypeLambda tyLam) {
            type = tyLam.body();
        }
        return type;
    }

    public static Type asmType(org.mina_lang.common.types.Type minaType) {
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
            return Type.getObjectType("org/mina_lang/runtime/Unit");
        } else if (org.mina_lang.common.types.Type.isFunction(minaType) &&
                minaType instanceof org.mina_lang.common.types.TypeApply tyApp) {
            return Type.getObjectType("org/mina_lang/runtime/Function" + (tyApp.typeArguments().size() - 1));
        } else if (minaType instanceof org.mina_lang.common.types.TypeVar tyVar) {
            return Type.getType(Object.class);
        } else if (minaType instanceof org.mina_lang.common.types.TypeApply tyApp) {
            return asmType(tyApp.type());
        } else if (minaType instanceof org.mina_lang.common.types.TypeConstructor tyCon) {
            return Type.getType(Names.getDescriptor(tyCon.name()));
        } else if (minaType instanceof org.mina_lang.common.types.TypeLambda tyLam) {
            return asmType(tyLam.body());
        } else if (minaType instanceof org.mina_lang.common.types.UnsolvedType unsolved) {
            return Type.getType(Object.class);
        }

        return null;
    }

    public static Type asmType(MetaNode<Attributes> node) {
        var minaType = getType(node);
        return asmType(minaType);
    }

    public static Type boxedAsmType(org.mina_lang.common.types.Type minaType) {
        if (minaType.equals(org.mina_lang.common.types.Type.BOOLEAN)) {
            return Type.getType(Boolean.class);
        } else if (minaType.equals(org.mina_lang.common.types.Type.CHAR)) {
            return Type.getType(Character.class);
        } else if (minaType.equals(org.mina_lang.common.types.Type.STRING)) {
            return Type.getType(String.class);
        } else if (minaType.equals(org.mina_lang.common.types.Type.INT)) {
            return Type.getType(Integer.class);
        } else if (minaType.equals(org.mina_lang.common.types.Type.LONG)) {
            return Type.getType(Long.class);
        } else if (minaType.equals(org.mina_lang.common.types.Type.FLOAT)) {
            return Type.getType(Float.class);
        } else if (minaType.equals(org.mina_lang.common.types.Type.DOUBLE)) {
            return Type.getType(Double.class);
        } else if (minaType.equals(org.mina_lang.common.types.Type.UNIT)) {
            return Type.getObjectType("org/mina_lang/runtime/Unit");
        } else if (org.mina_lang.common.types.Type.isFunction(minaType) &&
                minaType instanceof org.mina_lang.common.types.TypeApply tyApp) {
            return Type.getObjectType("org/mina_lang/runtime/Function" + (tyApp.typeArguments().size() - 1));
        } else if (minaType instanceof org.mina_lang.common.types.TypeVar tyVar) {
            return Type.getType(Object.class);
        } else if (minaType instanceof org.mina_lang.common.types.TypeApply tyApp) {
            return boxedAsmType(tyApp.type());
        } else if (minaType instanceof org.mina_lang.common.types.TypeConstructor tyCon) {
            return Type.getType(Names.getDescriptor(tyCon.name()));
        } else if (minaType instanceof org.mina_lang.common.types.TypeLambda tyLam) {
            return boxedAsmType(tyLam.body());
        } else if (minaType instanceof org.mina_lang.common.types.UnsolvedType unsolved) {
            return Type.getType(Object.class);
        }

        return null;
    }

    public static Type boxedAsmType(MetaNode<Attributes> node) {
        var minaType = getType(node);
        return boxedAsmType(minaType);
    }

    public static Method erasedMethod(String name, int arity) {
        var erasedArgs = new Type[arity];
        Arrays.fill(erasedArgs, OBJECT_TYPE);
        return new Method(name, OBJECT_TYPE, erasedArgs);
    }

    public static Type erasedMethodType(TypeApply funType) {
        var erasedArgs = new Type[funType.typeArguments().size() - 1];
        Arrays.fill(erasedArgs, OBJECT_TYPE);
        return Type.getMethodType(OBJECT_TYPE, erasedArgs);
    }

    public static Type boxedMethodType(TypeApply funType) {
        var boxedReturnType = Types.boxedAsmType(funType.typeArguments().getLast());
        var boxedArgTypes = funType.typeArguments()
                .take(funType.typeArguments().size() - 1)
                .collect(Types::boxedAsmType)
                .toArray(new Type[funType.typeArguments().size() - 1]);
        return Type.getMethodType(boxedReturnType, boxedArgTypes);
    }
}

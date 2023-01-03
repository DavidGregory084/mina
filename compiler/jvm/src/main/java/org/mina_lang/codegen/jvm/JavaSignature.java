package org.mina_lang.codegen.jvm;

import org.mina_lang.common.Attributes;
import org.mina_lang.syntax.ConstructorNode;
import org.mina_lang.syntax.DataNode;
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

        data.typeParams().forEach(tyParam -> {
            visitor.visitFormalTypeParameter(tyParam.name());
            var boundVisitor = visitor.visitClassBound();
            boundVisitor.visitClassType(OBJECT_NAME);
            boundVisitor.visitEnd();
        });

        var superclassVisitor = visitor.visitSuperclass();
        superclassVisitor.visitClassType(RECORD_NAME);
        superclassVisitor.visitEnd();

        var interfaceVisitor = visitor.visitInterface();

        interfaceVisitor.visitClassType(Names.getInternalName(data));

        data.typeParams().forEach(tyParam -> {
            var argVisitor = interfaceVisitor.visitTypeArgument(SignatureVisitor.INSTANCEOF);
            argVisitor.visitTypeVariable(tyParam.name());
        });

        interfaceVisitor.visitEnd();

        return visitor.toString();
    }
}

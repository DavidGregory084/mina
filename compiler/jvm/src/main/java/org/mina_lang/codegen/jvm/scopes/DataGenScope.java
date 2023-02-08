package org.mina_lang.codegen.jvm.scopes;

import static org.objectweb.asm.Opcodes.*;

import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.codegen.jvm.JavaSignature;
import org.mina_lang.codegen.jvm.Types;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.syntax.DataNode;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

public record DataGenScope(
        DataNode<Attributes> data,
        Type dataType,
        ClassWriter classWriter,
        MutableMap<String, Meta<Attributes>> values,
        MutableMap<String, Meta<Attributes>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<Attributes>>> fields) implements CodegenScope {
    public DataGenScope(
            DataNode<Attributes> data,
            Type dataType,
            ClassWriter classWriter) {
        this(
                data,
                dataType,
                classWriter,
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty());
    }

    public static DataGenScope open(DataNode<Attributes> data) {
        var classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        var dataType = Types.getDataAsmType(data);

        // Visit a new interface class which our constructors will implement
        classWriter.visit(
                V17,
                ACC_PUBLIC + ACC_INTERFACE + ACC_ABSTRACT,
                dataType.getInternalName(),
                JavaSignature.forData(data),
                Types.OBJECT_TYPE.getInternalName(),
                null);

        return new DataGenScope(data, dataType, classWriter);
    }

    public byte[] finaliseData() {
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }
}

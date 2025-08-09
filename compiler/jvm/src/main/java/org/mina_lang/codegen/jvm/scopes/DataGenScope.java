/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm.scopes;

import org.mina_lang.codegen.jvm.JavaSignature;
import org.mina_lang.codegen.jvm.Types;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.syntax.DataNode;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public record DataGenScope(
        DataNode<Attributes> data,
        Type dataType,
        ClassWriter classWriter,
        Map<String, Meta<Attributes>> values,
        Map<String, Meta<Attributes>> types,
        Map<ConstructorName, Map<String, Meta<Attributes>>> fields) implements CodegenScope {
    public DataGenScope(
            DataNode<Attributes> data,
            Type dataType,
            ClassWriter classWriter) {
        this(
                data,
                dataType,
                classWriter,
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>());
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

/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm.scopes;

import org.mina_lang.codegen.jvm.Asm;
import org.mina_lang.codegen.jvm.LocalVar;
import org.mina_lang.codegen.jvm.Types;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.Named;
import org.mina_lang.proto.ProtobufWriter;
import org.mina_lang.syntax.NamespaceNode;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.*;

public record NamespaceGenScope(
        NamespaceNode<Attributes> namespace,
        Type namespaceType,
        ClassWriter classWriter,
        GeneratorAdapter initWriter,
        Label startLabel,
        Label endLabel,
        Map<String, Meta<Attributes>> values,
        Map<String, Meta<Attributes>> types,
        Map<ConstructorName, Map<String, Meta<Attributes>>> fields,
        Map<Named, LocalVar> methodParams,
        Map<Named, LocalVar> localVars,
        AtomicInteger nextLambdaId)
        implements LambdaLiftingScope {

    public NamespaceGenScope(
            NamespaceNode<Attributes> namespace,
            Type namespaceType,
            ClassWriter classWriter,
            GeneratorAdapter initWriter,
            Label startLabel,
            Label endLabel) {
        this(
                namespace,
                namespaceType,
                classWriter,
                initWriter,
                startLabel,
                endLabel,
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                // Static initializer takes no params
                new HashMap<>(),
                new HashMap<>(),
                new AtomicInteger());
    }

    @Override
    public GeneratorAdapter methodWriter() {
        return initWriter();
    }

    public static NamespaceGenScope open(NamespaceNode<Attributes> namespace, ProtobufWriter protobufWriter) {
        var classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        var initWriter = Asm.staticInitializer(classWriter);
        var namespaceType = Types.getNamespaceAsmType(namespace);

        // Visit a new final class to contain the static methods of our namespace
        classWriter.visit(
                V17,
                ACC_PUBLIC + ACC_FINAL + ACC_SUPER,
                namespaceType.getInternalName(),
                null,
                Types.OBJECT_TYPE.getInternalName(),
                null);

        // Visit the custom MinaEnvironment attribute which stores information
        // about values, types and constructor fields in the namespace
        var proto = protobufWriter.toProto(namespace.getScope());
        classWriter.visitAttribute(new Asm.EnvironmentAttribute(proto.toByteArray()));

        // Start and end labels for variable debug info of the static initializer
        var startLabel = new Label();
        var endLabel = new Label();

        initWriter.visitCode();
        initWriter.visitLabel(startLabel);

        return new NamespaceGenScope(
                namespace,
                namespaceType,
                classWriter,
                initWriter,
                startLabel, endLabel);
    }

    public byte[] finaliseNamespace() {
        // Finish static initializer
        finaliseMethod();
        // Finish writing class
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }
}

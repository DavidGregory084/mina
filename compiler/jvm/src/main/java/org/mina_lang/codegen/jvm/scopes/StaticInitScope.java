/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm.scopes;

import org.mina_lang.codegen.jvm.Asm;
import org.mina_lang.codegen.jvm.LocalVar;
import org.mina_lang.codegen.jvm.Names;
import org.mina_lang.codegen.jvm.Types;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.LetName;
import org.mina_lang.common.names.Named;
import org.mina_lang.syntax.LetNode;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

public record StaticInitScope(
        LetNode<Attributes> let,
        GeneratorAdapter initWriter,
        GeneratorAdapter methodWriter,
        Label startLabel,
        Label endLabel,
        Map<String, Meta<Attributes>> values,
        Map<String, Meta<Attributes>> types,
        Map<ConstructorName, Map<String, Meta<Attributes>>> fields,
        Map<Named, LocalVar> methodParams,
        Map<Named, LocalVar> localVars) implements JavaMethodScope {

    public StaticInitScope(
            LetNode<Attributes> let,
            GeneratorAdapter initWriter,
            GeneratorAdapter methodWriter,
            Label startLabel,
            Label endLabel) {
        this(let,
                initWriter,
                methodWriter,
                startLabel,
                endLabel,
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>());
    }

    public static StaticInitScope open(
            LetNode<Attributes> let,
            GeneratorAdapter initWriter,
            ClassWriter namespaceWriter) {

        var methodWriter = Asm.methodWriter(
                ACC_STATIC + ACC_PRIVATE,
                "init$" + let.name(),
                Types.asmType(let),
                List.of(),
                null,
                namespaceWriter);

        var startLabel = new Label();
        var endLabel = new Label();

        methodWriter.visitCode();
        methodWriter.visitLabel(startLabel);

        return new StaticInitScope(let, initWriter, methodWriter, startLabel, endLabel);
    }

    public void finaliseInit() {
        var letName = (LetName) Names.getName(let);
        var namespaceType = Types.getNamespaceAsmType(letName.name().ns());
        var initMethod = new Method(
                methodWriter().getName(),
                methodWriter().getReturnType(),
                methodWriter().getArgumentTypes());
        finaliseMethod();
        initWriter().invokeStatic(namespaceType, initMethod);
        initWriter().putStatic(namespaceType, let.name(), Types.asmType(let));
    }
}

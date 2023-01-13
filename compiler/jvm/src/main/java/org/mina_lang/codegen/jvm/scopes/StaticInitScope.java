package org.mina_lang.codegen.jvm.scopes;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
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
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

public record StaticInitScope(
        LetNode<Attributes> let,
        GeneratorAdapter initWriter,
        GeneratorAdapter methodWriter,
        Label startLabel,
        Label endLabel,
        MutableMap<String, Meta<Attributes>> values,
        MutableMap<String, Meta<Attributes>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<Attributes>>> fields,
        ImmutableMap<Named, LocalVar> methodParams,
        MutableMap<Named, LocalVar> localVars) implements JavaMethodScope {

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
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.immutable.empty(),
                Maps.mutable.empty());
    }

    public static StaticInitScope open(
            LetNode<Attributes> let,
            GeneratorAdapter initWriter,
            ClassWriter namespaceWriter) {

        var methodWriter = Asm.methodWriter(
                ACC_STATIC + ACC_PRIVATE,
                "init$" + let.name(),
                Type.VOID_TYPE,
                Lists.immutable.empty(),
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
        methodWriter().putStatic(namespaceType, let.name(), Types.asmType(let));
        finaliseMethod();
        initWriter().invokeStatic(namespaceType, initMethod);
    }
}

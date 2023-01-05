package org.mina_lang.codegen.jvm.scopes;

import static org.objectweb.asm.Opcodes.*;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.mina_lang.codegen.jvm.*;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.Named;
import org.mina_lang.syntax.ConstructorNode;
import org.mina_lang.syntax.DataNode;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

public record ConstructorGenScope(
        ConstructorNode<Attributes> constr,
        Type constrType,
        ClassWriter classWriter,
        GeneratorAdapter initWriter,
        Label startLabel,
        Label endLabel,
        MutableMap<String, Meta<Attributes>> values,
        MutableMap<String, Meta<Attributes>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<Attributes>>> fields,
        ImmutableMap<Named, LocalVar> methodParams,
        MutableMap<Named, LocalVar> localVars) implements JavaMethodScope {

    public ConstructorGenScope(
            ConstructorNode<Attributes> constr,
            Type constrType,
            ClassWriter classWriter,
            GeneratorAdapter initWriter,
            Label startLabel,
            Label endLabel,
            ImmutableMap<Named, LocalVar> methodParams) {
        this(
                constr,
                constrType,
                classWriter,
                initWriter,
                startLabel,
                endLabel,
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                methodParams,
                Maps.mutable.empty());
    }

    @Override
    public GeneratorAdapter methodWriter() {
        return initWriter();
    }

    public void visitThisVar() {
        initWriter.visitLocalVariable(
                "this",
                constrType.getDescriptor(),
                JavaSignature.forConstructorInstance(constr),
                startLabel,
                endLabel,
                0);
    }

    @Override
    public void visitLocalVars() {
        visitThisVar();
        JavaMethodScope.super.visitLocalVars();
    }

    public static ConstructorGenScope open(ConstructorNode<Attributes> constructor, DataNode<Attributes> data) {
        var classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        var initWriter = Asm.constructor(
                classWriter,
                JavaSignature.forJavaConstructor(constructor),
                constructor.params());

        var constrType = Types.getConstructorAsmType(constructor);

        // Visit a new record class implementing our data type interface
        classWriter.visit(
                V17,
                ACC_PUBLIC + ACC_FINAL + ACC_SUPER + ACC_RECORD,
                constrType.getInternalName(),
                JavaSignature.forConstructor(data, constructor),
                Types.RECORD_TYPE.getInternalName(),
                new String[] { Types.getDataAsmType(data).getInternalName() });

        // Start and end labels for variable debug info of the Java constructor
        var startLabel = new Label();
        var endLabel = new Label();

        var methodParams = constructor.params()
                .collectWithIndex((param, index) -> {
                    var paramName = Names.getName(param);
                    var paramMinaType = Types.getType(param);
                    var paramType = Types.asmType(param);
                    var paramSignature = JavaSignature.forType(paramMinaType);
                    return Tuples.pair(
                            paramName,
                            new LocalVar(
                                    ACC_FINAL,
                                    index + 1, // first param is `this`
                                    param.name(),
                                    paramType.getDescriptor(),
                                    paramSignature,
                                    startLabel,
                                    endLabel));
                }).toImmutableMap(Pair::getOne, Pair::getTwo);

        methodParams
                .toSortedListBy(LocalVar::index)
                .forEach(param -> {
                    initWriter.visitParameter(param.name(), param.access());
                });

        initWriter.visitCode();
        initWriter.visitLabel(startLabel);

        // Invoke superclass constructor
        initWriter.loadThis();
        initWriter.invokeConstructor(
                Types.RECORD_TYPE,
                new Method("<init>", Type.getMethodDescriptor(Type.VOID_TYPE)));

        return new ConstructorGenScope(
                constructor,
                constrType,
                classWriter,
                initWriter,
                startLabel, endLabel,
                methodParams);
    }

    public byte[] finaliseConstructor() {
        // Finish Java constructor
        finaliseMethod();
        // Finish writing class
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }
}

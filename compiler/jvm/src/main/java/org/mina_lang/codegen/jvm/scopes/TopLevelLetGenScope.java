/*
 * SPDX-FileCopyrightText:  © 2023-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm.scopes;

import it.unimi.dsi.fastutil.Pair;
import org.mina_lang.codegen.jvm.*;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.LocalName;
import org.mina_lang.common.names.Named;
import org.mina_lang.common.types.TypeApply;
import org.mina_lang.syntax.LambdaNode;
import org.mina_lang.syntax.LetFnNode;
import org.mina_lang.syntax.LetNode;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.objectweb.asm.Opcodes.*;

public record TopLevelLetGenScope(
        GeneratorAdapter methodWriter,
        Label startLabel,
        Label endLabel,
        Map<String, Meta<Attributes>> values,
        Map<String, Meta<Attributes>> types,
        Map<ConstructorName, Map<String, Meta<Attributes>>> fields,
        Map<Named, LocalVar> methodParams,
        Map<Named, LocalVar> localVars,
        AtomicInteger nextLambdaId) implements LambdaLiftingScope {

    public TopLevelLetGenScope(
            GeneratorAdapter methodWriter,
            Label startLabel,
            Label endLabel,
            Map<Named, LocalVar> methodParams) {
        this(
                methodWriter,
                startLabel,
                endLabel,
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                methodParams,
                new HashMap<>(),
                new AtomicInteger(0));
    }

    public static TopLevelLetGenScope open(
            LetFnNode<Attributes> letFn,
            ClassWriter namespaceWriter) {
        var methodWriter = Asm.methodWriter(
                letFn.name(),
                letFn.expr(),
                letFn.valueParams().stream().map(Types::asmType).toList(),
                JavaSignature.forMethod(letFn),
                namespaceWriter);

        var startLabel = new Label();
        var endLabel = new Label();

        var methodParams = IntStream.range(0, letFn.valueParams().size())
            .mapToObj(index -> {
                var param = letFn.valueParams().get(index);
                var paramName = Names.getName(param);
                var paramMinaType = Types.getType(param);
                var paramType = Types.asmType(paramMinaType);
                var paramSignature = JavaSignature.forType(paramMinaType);
                return Pair.of(
                    paramName,
                    new LocalVar(
                        ACC_FINAL,
                        index,
                        param.name(),
                        paramType.getDescriptor(),
                        paramSignature,
                        startLabel,
                        endLabel));
            }).collect(Collectors.toMap(Pair::first, Pair::second));

        methodParams.values().stream()
            .sorted(Comparator.comparingInt(LocalVar::index))
            .forEach(param -> {
                methodWriter.visitParameter(param.name(), param.access());
            });

        methodWriter.visitCode();
        methodWriter.visitLabel(startLabel);

        return new TopLevelLetGenScope(methodWriter, startLabel, endLabel, methodParams);
    }

    public static TopLevelLetGenScope open(
            LetNode<Attributes> let,
            LambdaNode<Attributes> lambda,
            ClassWriter namespaceWriter) {
        // Top-level lambdas become static methods
        var methodWriter = Asm.methodWriter(
                let.name(),
                lambda.body(),
                lambda.params().stream().map(Types::asmType).toList(),
                JavaSignature.forMethod(let),
                namespaceWriter);

        var startLabel = new Label();
        var endLabel = new Label();

        var methodParams = IntStream.range(0, lambda.params().size()).mapToObj(index -> {
            var param = lambda.params().get(index);
            var paramName = Names.getName(param);
            var paramMinaType = Types.getType(param);
            var paramType = Types.asmType(paramMinaType);
            var paramSignature = JavaSignature.forType(paramMinaType);
            return Pair.of(
                paramName,
                new LocalVar(
                    ACC_FINAL,
                    index,
                    param.name(),
                    paramType.getDescriptor(),
                    paramSignature,
                    startLabel,
                    endLabel));
        }).collect(Collectors.toMap(Pair::first, Pair::second));

        methodParams.values().stream()
            .sorted(Comparator.comparingInt(LocalVar::index))
            .forEach(param -> {
                methodWriter.visitParameter(param.name(), param.access());
            });

        methodWriter.visitCode();
        methodWriter.visitLabel(startLabel);

        return new TopLevelLetGenScope(methodWriter, startLabel, endLabel, methodParams);
    }

    public static TopLevelLetGenScope open(
            LetNode<Attributes> let,
            ClassWriter namespaceWriter) {
        var funType = (TypeApply) Types.getUnderlyingType(let);

        var argMinaTypes = funType.typeArguments().subList(0, funType.typeArguments().size() - 1);

        var returnType = Types.asmType(funType.typeArguments().get(funType.typeArguments().size() - 1));

        // Top-level eta-reduced functions are adapted into static methods
        var methodWriter = Asm.methodWriter(
                ACC_PUBLIC + ACC_STATIC,
                let.name(),
                returnType,
                argMinaTypes.stream().map(Types::asmType).toList(),
                JavaSignature.forMethod(let),
                namespaceWriter);

        var startLabel = new Label();
        var endLabel = new Label();

        var methodParams = IntStream.range(0, argMinaTypes.size()).mapToObj(index -> {
            var paramMinaType = argMinaTypes.get(index);
            var paramType = Types.asmType(paramMinaType);
            var paramSignature = JavaSignature.forType(paramMinaType);
            return Pair.of(
                (Named) new LocalName("arg" + index, 0),
                new LocalVar(
                    // These params are created to adapt the eta-reduced function
                    ACC_FINAL + ACC_SYNTHETIC,
                    index,
                    "arg" + index,
                    paramType.getDescriptor(),
                    paramSignature,
                    startLabel,
                    endLabel));
        }).collect(Collectors.toMap(Pair::first, Pair::second));

        methodParams.values().stream()
            .sorted(Comparator.comparingInt(LocalVar::index))
            .forEach(param -> {
                methodWriter.visitParameter(param.name(), param.access());
            });

        methodWriter.visitCode();
        methodWriter.visitLabel(startLabel);

        return new TopLevelLetGenScope(methodWriter, startLabel, endLabel, methodParams);
    }

    public void finaliseLet() {
        finaliseMethod();
    }
}

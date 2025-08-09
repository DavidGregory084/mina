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
import org.mina_lang.common.names.Named;
import org.mina_lang.syntax.LambdaNode;
import org.mina_lang.syntax.MetaNode;
import org.mina_lang.syntax.ReferenceNode;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.objectweb.asm.Opcodes.*;

public record LambdaGenScope(
        GeneratorAdapter methodWriter,
        Label startLabel,
        Label endLabel,
        Map<String, Meta<Attributes>> values,
        Map<String, Meta<Attributes>> types,
        Map<ConstructorName, Map<String, Meta<Attributes>>> fields,
        Map<Named, LocalVar> methodParams,
        Map<Named, LocalVar> localVars,
        List<ReferenceNode<Attributes>> freeVariables) implements JavaMethodScope {
    public LambdaGenScope(GeneratorAdapter methodWriter, Label startLabel, Label endLabel,
            Map<Named, LocalVar> methodParams, List<ReferenceNode<Attributes>> freeVariables) {
        this(
                methodWriter,
                startLabel,
                endLabel,
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                methodParams,
                new HashMap<>(),
                freeVariables);
    }

    public static LambdaGenScope open(
            LambdaLiftingScope enclosingLifter,
            LambdaNode<Attributes> lambda,
            ClassWriter namespaceWriter) {

        var freeVariables = lambda.accept(new FreeLocalVariablesFolder());

        // Any free variables captured in the lambda must be converted into parameters
        var allParams = new ArrayList<MetaNode<Attributes>>();
        allParams.addAll(freeVariables);
        allParams.addAll(lambda.params());

        var enclosingMethodName = enclosingLifter.methodWriter().getName();

        var liftedMethodName = "<clinit>".equals(enclosingMethodName) ? "static" : enclosingMethodName;

        var liftedMethodId = enclosingLifter.nextLambdaId().getAndIncrement();

        // Write out a new static method to implement the lambda
        var methodWriter = Asm.methodWriter(
            ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC,
            "lambda$" + liftedMethodName + "$" + liftedMethodId,
            lambda.body(),
            allParams.stream().map(Types::asmType).toList(),
            null,
            namespaceWriter);

        var startLabel = new Label();
        var endLabel = new Label();

        var methodParams = IntStream.range(0, allParams.size())
            .mapToObj(index -> {
                var param = allParams.get(index);
                var paramName = Names.getName(param);
                var paramType = Types.asmType(param);
                // Free variables prepended to the parameter list are marked synthetic
                var syntheticParam = index >= freeVariables.size() ? ACC_SYNTHETIC : 0;
                return Pair.of(
                    paramName,
                    new LocalVar(
                        ACC_FINAL + syntheticParam,
                        index,
                        paramName.localName(),
                        paramType.getDescriptor(),
                        null,
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

        return new LambdaGenScope(
                methodWriter,
                startLabel,
                endLabel,
                methodParams,
                freeVariables);
    }

    public void finaliseLambda() {
        finaliseMethod();
    }
}

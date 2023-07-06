/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm.scopes;

import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.tuple.Tuples;
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

import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.*;

public record TopLevelLetGenScope(
        GeneratorAdapter methodWriter,
        Label startLabel,
        Label endLabel,
        MutableMap<String, Meta<Attributes>> values,
        MutableMap<String, Meta<Attributes>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<Attributes>>> fields,
        ImmutableMap<Named, LocalVar> methodParams,
        MutableMap<Named, LocalVar> localVars,
        AtomicInteger nextLambdaId) implements LambdaLiftingScope {

    public TopLevelLetGenScope(
            GeneratorAdapter methodWriter,
            Label startLabel,
            Label endLabel,
            ImmutableMap<Named, LocalVar> methodParams) {
        this(
                methodWriter,
                startLabel,
                endLabel,
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                methodParams,
                Maps.mutable.empty(),
                new AtomicInteger(0));
    }

    public static TopLevelLetGenScope open(
            LetFnNode<Attributes> letFn,
            ClassWriter namespaceWriter) {
        var methodWriter = Asm.methodWriter(
                letFn.name(),
                letFn.expr(),
                letFn.valueParams().collect(Types::asmType),
                JavaSignature.forMethod(letFn),
                namespaceWriter);

        var startLabel = new Label();
        var endLabel = new Label();

        var methodParams = letFn.valueParams().collectWithIndex((param, index) -> {
            var paramName = Names.getName(param);
            var paramMinaType = Types.getType(param);
            var paramType = Types.asmType(paramMinaType);
            var paramSignature = JavaSignature.forType(paramMinaType);
            return Tuples.pair(
                    paramName,
                    new LocalVar(
                            ACC_FINAL,
                            index,
                            param.name(),
                            paramType.getDescriptor(),
                            paramSignature,
                            startLabel,
                            endLabel));
        }).toImmutableMap(Pair::getOne, Pair::getTwo);

        methodParams
                .toSortedListBy(LocalVar::index)
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
                lambda.params().collect(Types::asmType),
                JavaSignature.forMethod(let),
                namespaceWriter);

        var startLabel = new Label();
        var endLabel = new Label();

        var methodParams = lambda.params().collectWithIndex((param, index) -> {
            var paramName = Names.getName(param);
            var paramMinaType = Types.getType(param);
            var paramType = Types.asmType(paramMinaType);
            var paramSignature = JavaSignature.forType(paramMinaType);
            return Tuples.pair(
                    paramName,
                    new LocalVar(
                            ACC_FINAL,
                            index,
                            param.name(),
                            paramType.getDescriptor(),
                            paramSignature,
                            startLabel,
                            endLabel));
        }).toImmutableMap(Pair::getOne, Pair::getTwo);

        methodParams
                .toSortedListBy(LocalVar::index)
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

        var argMinaTypes = funType.typeArguments()
                .take(funType.typeArguments().size() - 1);

        var returnType = Types.boxedAsmType(funType.typeArguments().getLast());

        // Top-level eta-reduced functions are adapted into static methods
        var methodWriter = Asm.methodWriter(
                ACC_PUBLIC + ACC_STATIC,
                let.name(),
                returnType,
                argMinaTypes.collect(Types::boxedAsmType),
                JavaSignature.forMethod(let),
                namespaceWriter);

        var startLabel = new Label();
        var endLabel = new Label();

        var methodParams = argMinaTypes.collectWithIndex((paramMinaType, index) -> {
            var paramType = Types.asmType(paramMinaType);
            var paramSignature = JavaSignature.forType(paramMinaType);
            return Tuples.pair(
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
        }).toImmutableMap(Pair::getOne, Pair::getTwo);

        methodParams
                .toSortedListBy(LocalVar::index)
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

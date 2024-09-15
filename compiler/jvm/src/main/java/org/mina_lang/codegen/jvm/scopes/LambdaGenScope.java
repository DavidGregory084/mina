/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm.scopes;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.tuple.Tuples;
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

import static org.objectweb.asm.Opcodes.*;

public record LambdaGenScope(
        GeneratorAdapter methodWriter,
        Label startLabel,
        Label endLabel,
        MutableMap<String, Meta<Attributes>> values,
        MutableMap<String, Meta<Attributes>> types,
        MutableMap<ConstructorName, MutableMap<String, Meta<Attributes>>> fields,
        ImmutableMap<Named, LocalVar> methodParams,
        MutableMap<Named, LocalVar> localVars,
        ImmutableList<ReferenceNode<Attributes>> freeVariables) implements JavaMethodScope {
    public LambdaGenScope(GeneratorAdapter methodWriter, Label startLabel, Label endLabel,
            ImmutableMap<Named, LocalVar> methodParams, ImmutableList<ReferenceNode<Attributes>> freeVariables) {
        this(
                methodWriter,
                startLabel,
                endLabel,
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                Maps.mutable.empty(),
                methodParams,
                Maps.mutable.empty(),
                freeVariables);
    }

    public static LambdaGenScope open(
            LambdaLiftingScope enclosingLifter,
            LambdaNode<Attributes> lambda,
            ClassWriter namespaceWriter) {

        var freeVariables = lambda.accept(new FreeVariablesFolder());

        // Any free variables captured in the lambda must be converted into parameters
        var allParams = Lists.immutable.<MetaNode<Attributes>>empty()
                .newWithAll(freeVariables)
                .newWithAll(lambda.params());

        var enclosingMethodName = enclosingLifter.methodWriter().getName();

        var liftedMethodName = "<clinit>".equals(enclosingMethodName) ? "static" : enclosingMethodName;

        var liftedMethodId = enclosingLifter.nextLambdaId().getAndIncrement();

        // Write out a new static method to implement the lambda
        var methodWriter = Asm.methodWriter(
                ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC,
                "lambda$" + liftedMethodName + "$" + liftedMethodId,
                lambda.body(),
                allParams.collect(Types::asmType),
                null,
                namespaceWriter);

        var startLabel = new Label();
        var endLabel = new Label();

        var methodParams = allParams.collectWithIndex((param, index) -> {
            var paramName = Names.getName(param);
            var paramType = Types.asmType(param);
            // Free variables prepended to the parameter list are marked synthetic
            var syntheticParam = index >= freeVariables.size() ? ACC_SYNTHETIC : 0;
            return Tuples.pair(
                    paramName,
                    new LocalVar(
                            ACC_FINAL + syntheticParam,
                            index,
                            paramName.localName(),
                            paramType.getDescriptor(),
                            null,
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

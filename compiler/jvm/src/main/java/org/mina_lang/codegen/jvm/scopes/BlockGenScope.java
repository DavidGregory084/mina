/*
 * SPDX-FileCopyrightText:  © 2023 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm.scopes;

import org.mina_lang.codegen.jvm.LocalVar;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.names.ConstructorName;
import org.mina_lang.common.names.Named;
import org.mina_lang.syntax.BlockNode;
import org.objectweb.asm.Label;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.HashMap;
import java.util.Map;

public record BlockGenScope(
        JavaMethodScope enclosingMethod,
        Label startLabel,
        Label endLabel,
        Map<String, Meta<Attributes>> values,
        Map<String, Meta<Attributes>> types,
        Map<ConstructorName, Map<String, Meta<Attributes>>> fields,
        Map<Named, LocalVar> localVars) implements VarBindingScope {
    public BlockGenScope(JavaMethodScope enclosingMethod, Label startLabel, Label endLabel) {
        this(
                enclosingMethod,
                startLabel,
                endLabel,
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>());
    }

    @Override
    public GeneratorAdapter methodWriter() {
        return enclosingMethod.methodWriter();
    }

    public static BlockGenScope open(JavaMethodScope enclosingMethod, BlockNode<Attributes> block) {
        var startLabel = new Label();
        var endLabel = new Label();

        enclosingMethod.methodWriter().visitLabel(startLabel);

        var blockScope = new BlockGenScope(enclosingMethod, startLabel, endLabel);

        block.declarations().forEach(decl -> {
            blockScope.putLocalVar(decl, startLabel, endLabel);
        });

        return blockScope;
    }

    public void finaliseBlock() {
        enclosingMethod
                .methodWriter()
                .visitLabel(endLabel);
        visitLocalVars();
    }
}
